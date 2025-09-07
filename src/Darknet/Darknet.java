package Darknet;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.HashMap;
import java.util.Map;

public class Darknet
{
	// For a summary of memory layout, downcall handles, and upcall subs, see this SO post:  https://stackoverflow.com/a/79521845/13022

	/** NETWORKPTR is a Darknet::NetworkPtr, which is an opaque C-style void pointer to a structure internal to Darknet.
	 * We don't actually care about the content of the pointer.  But we know we'll need to pass this pointer every time
	 * a call is made into Darknet/YOLO.
	 */
	public static final AddressLayout NETWORKPTR = ValueLayout.ADDRESS;

	/// C_POINTER is a generic C-style pointer, without knowing how many bytes will be referenced.
	public static final AddressLayout C_POINTER = ValueLayout.ADDRESS;//.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE));


	private Arena arena;
	private Linker linker;
	private SymbolLookup lookup;
	private MemorySegment networkptr;


	public Darknet()
	{
		System.loadLibrary("darknet");	// looks for libdarknet.so in Linux or darknet.dll in Windows
		this.arena		= Arena.ofShared();
		this.linker		= Linker.nativeLinker();
		this.lookup		= SymbolLookup.loaderLookup();
		this.networkptr	= null;
	}


	/** Run a function that takes no parameters and returns nothing (void).  An example of that would be
	 * @p darknet_show_version_info() which takes no parameters and returns nothing.
	 */
	private void run_v(String name) throws Throwable
	{
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid());
			handle.invokeExact();
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s()\"%n", name);
			e.printStackTrace();
			throw e;
		}
		return;
	}


	/// Run a function that takes no parameters and returns a text string.
	private String run_s(String name) throws Throwable
	{
		String str = new String();
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.of(ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE))));
			MemorySegment result = (MemorySegment) handle.invokeExact();
			str = result.getString(0);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s()\"%n", name);
			e.printStackTrace();
			throw e;
		}
		return str;
	}


	/// Run a function that takes a boolean and returns nothing.
	private void run_b(String name, boolean flag) throws Throwable
	{
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid(ValueLayout.JAVA_BOOLEAN));
			handle.invokeExact(flag);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s(%b)\"%n", name, flag);
			e.printStackTrace();
			throw e;
		}
		return;
	}


	private void run_f(String name, float f) throws Throwable
	{
		try
		{
			if (networkptr == null)
			{
				throw new NullPointerException("network pointer is null (neural network is not loaded)");
			}

			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid(NETWORKPTR, ValueLayout.JAVA_FLOAT));
			handle.invokeExact(networkptr, f);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s(%f)\"%n", name, f);
			e.printStackTrace();
			throw e;
		}
		return;
	}


	/// Run a function that takes a string and returns nothing.
	private void run_str(String name, String str) throws Throwable
	{
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid(C_POINTER));
			MemorySegment ms = string_to_memorysegment(str);
			handle.invokeExact(ms);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s(%s)\"%n", name, str);
			e.printStackTrace();
			throw e;
		}
		return;
	}


	public MemorySegment string_to_memorysegment(String str)
	{
		MemorySegment seg;

		if (true)
		{
			byte[] bytes = str.getBytes(java.nio.charset.StandardCharsets.UTF_8);
			seg = arena.allocate(bytes.length + 1);
			seg.asByteBuffer().put(bytes);
			seg.set(ValueLayout.JAVA_BYTE, bytes.length, (byte)0);
		}
		else
		{
			seg = arena.allocateFrom(str, java.nio.charset.StandardCharsets.UTF_8);
		}

//		System.out.format("string %s converted to memory segment: %s%n", str, seg);

		return seg;
	}


	/** Display version information on @p STDOUT. Example:
	 *  ````
	 *  Darknet V4 "Slate" v4.0-51-g53faaf9c
	 *  Darknet is compiled to use the CPU. GPU is disabled.
	 *  OpenCV v4.10.0, Windows 11 Home
	 *  ````
	 *  @see @ref version_string()
	 */
	public void show_version_info() throws Throwable
	{
		run_v("darknet_show_version_info");
	}


	public String version_string() throws Throwable
	{
		return run_s("darknet_version_string");
	}


	public String version_short() throws Throwable
	{
		return run_s("darknet_version_short");
	}


	public void set_verbose(boolean flag) throws Throwable
	{
		run_b("darknet_set_verbose", flag);
	}


	public void set_trace(boolean flag) throws Throwable
	{
		run_b("darknet_set_trace", flag);
	}


	public MemorySegment load_neural_network(String config, String names, String weights) throws Throwable
	{
		networkptr = null;
		try
		{
			MemorySegment fn1 = string_to_memorysegment(config);
			MemorySegment fn2 = string_to_memorysegment(names);
			MemorySegment fn3 = string_to_memorysegment(weights);

			MemorySegment function = lookup.findOrThrow("darknet_load_neural_network");
			FunctionDescriptor descriptor = FunctionDescriptor.of(NETWORKPTR, C_POINTER, C_POINTER, C_POINTER);
			MethodHandle handle = linker.downcallHandle(function, descriptor);
			networkptr = (MemorySegment) handle.invokeExact(fn1, fn2, fn3);

//			System.out.format("neural network loaded .. %s%n", networkptr);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running darknet_load_neural_network(%s)%n", config);
			e.printStackTrace();
			throw e;
		}
		return networkptr;
	}


	public void free_neural_network() throws Throwable
	{
		if (networkptr != null)
		{
			try
			{
				// special case -- we need a *pointer* to the networkptr, so we need to wrap it in another layer
				MemorySegment ptr = arena.allocate(ValueLayout.ADDRESS);
				ptr.set(ValueLayout.ADDRESS, 0, networkptr);

				MemorySegment function = lookup.findOrThrow("darknet_free_neural_network");
				FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS);
				MethodHandle handle = linker.downcallHandle(function, descriptor);
				handle.invokeExact(ptr);

				networkptr = null;
			}
			catch (Throwable e)
			{
				System.out.format("Exception caught while running darknet_free_neural_network()%n");
				e.printStackTrace();
				throw e;
			}
		}
	}


	public void set_output_stream(String output_filename) throws Throwable
	{
		run_str("darknet_set_output_stream", output_filename);
	}


	public void set_detection_threshold(float threshold) throws Throwable
	{
		run_f("darknet_set_detection_threshold", threshold);
	}


	public void set_non_maximal_suppression_threshold(float threshold) throws Throwable
	{
		run_f("darknet_set_non_maximal_suppression_threshold", threshold);
	}


	public Map<String, Integer> network_dimensions() throws Throwable
	{
		Map<String, Integer> results = new HashMap<>();

		try
		{
			if (networkptr == null)
			{
				throw new NullPointerException("network pointer is null (neural network is not loaded)");
			}

			MemorySegment width		= arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment height	= arena.allocate(ValueLayout.JAVA_INT);
			MemorySegment channel	= arena.allocate(ValueLayout.JAVA_INT);

			MemorySegment function = lookup.findOrThrow("darknet_network_dimensions");
			FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(NETWORKPTR, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
			MethodHandle handle = linker.downcallHandle(function, descriptor);
			handle.invokeExact(networkptr, width, height, channel);

			results.put("width"		, width		.get(ValueLayout.JAVA_INT, 0));
			results.put("height"	, height	.get(ValueLayout.JAVA_INT, 0));
			results.put("channel"	, channel	.get(ValueLayout.JAVA_INT, 0));
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running darknet_network_dimensions()%n");
			e.printStackTrace();
			throw e;
		}

		return results;
	}

}
