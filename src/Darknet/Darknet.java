package Darknet;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.HashMap;
import java.util.Map;

/** This class allows you to work with Darknet/YOLO neural networks.
 * @since 2025-09-09
 */
public class Darknet
{
	// For a summary of memory layout, downcall handles, and upcall subs, see this SO post:  https://stackoverflow.com/a/79521845/13022

	/** NETWORKPTR is a Darknet::NetworkPtr, which is an opaque C-style void pointer to a structure internal to %Darknet.
	 * We don't actually care about the content of the pointer.  But we know we'll need to pass this pointer every time
	 * a call is made into Darknet/YOLO.
	 */
	private static final AddressLayout NETWORKPTR = ValueLayout.ADDRESS;

	/// C_POINTER is a generic C-style pointer, without knowing how many bytes will be referenced.
	private static final AddressLayout C_POINTER = ValueLayout.ADDRESS;//.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE));


	private Arena arena;
	private Linker linker;
	private SymbolLookup lookup;
	private MemorySegment networkptr;


	/** Constructor.
	 * @see @ref Darknet.Darknet.load_neural_network()
	 * @since 2025-09-09
	 */
	public Darknet()
	{
		System.loadLibrary("darknet");	// looks for libdarknet.so in Linux or darknet.dll in Windows
		this.arena		= Arena.ofShared();
		this.linker		= Linker.nativeLinker();
		this.lookup		= SymbolLookup.loaderLookup();
		this.networkptr	= null;
	}


	/** Convert a Java string to a @p MemorySegment for use with FFM.
	 * @since 2025-09-09
	 */
	private MemorySegment string_to_memorysegment(String str)
	{
		MemorySegment seg;

		// not sure which is better, or if there is a difference between these 2 techniques
		if (false)
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


	/** Run a function that takes zero parameters and returns nothing (void).
	 * An example of that would be @ref Darknet.Darknet.show_version_info() which takes no parameters and returns nothing.
	 * @since 2025-09-09
	 */
	private void run_void(String name) throws Throwable
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


	/** Run a function that takes zero parameters and returns a text string.
	 * An example of that would be @ref Darknet.Darknet.version_string().
	 * @since 2025-09-09
	 */
	private String get_string(String name) throws Throwable
	{
		String str = new String();
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.of(ValueLayout.ADDRESS));
			MemorySegment result = (MemorySegment) handle.invokeExact();
			str = result.reinterpret(Long.MAX_VALUE).getString(0);
		}
		catch (Throwable e)
		{
			System.out.format("Exception caught while running \"%s()\"%n", name);
			e.printStackTrace();
			throw e;
		}
		return str;
	}


	/** Run a function that takes a boolean and returns nothing.
	 * An example of that would be @ref Darknet.Darknet.set_verbose().
	 * @since 2025-09-09
	 */
	private void set_bool(String name, boolean flag) throws Throwable
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


	/** Run a function that takes the network pointer and a float, and returns nothing.
	 * An example of that would be @ref Darknet.Darknet.set_detection_threshold().
	 * @since 2025-09-09
	 */
	private void set_netptr_float(String name, float f) throws Throwable
	{
		try
		{
			if (networkptr == null)
			{
				throw new NullPointerException("network pointer is null (neural network is not loaded?), cannot run \"" + name + "\"");
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


	/** Run a function that takes a string, and returns nothing.
	 * An example of that would be @ref Darknet.Darknet.set_output_stream().
	 * @since 2025-09-09
	 */
	private void set_string(String name, String str) throws Throwable
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


	/** Display version information on @p STDOUT.
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 *
	 * Example:
	 * ```
	 * Darknet V5 "Moonlit" v5.0-138-ga061d2f0 [v5]
	 * CUDA runtime version 12000 (v12.0), driver version 12060 (v12.6)
	 * cuDNN version 12020 (v8.9.7), use of half-size floats is ENABLED
	 * => 0: NVIDIA GeForce RTX 3090 [#8.6], 23.6 GiB
	 * Protobuf 3.21.12, OpenCV 4.6.0, Ubuntu 24.04
	 * ```
	 *
	 * @see @ref Darknet.Darknet.version_string()
	 * @see @ref Darknet.Darknet.version_short()
	 * @since 2025-09-09
	 */
	public void show_version_info() throws Throwable
	{
		run_void("darknet_show_version_info");
	}


	/** Get the full version string.  For example, @p "v5.0-138-ga061d2f0".
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 * @see @ref Darknet.Darknet.version_short()
	 * @since 2025-09-09
	 */
	public String version_string() throws Throwable
	{
		return get_string("darknet_version_string");
	}


	/** Get the short version string.  For example, @p "5.0.138".
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 * @see @ref Darknet.Darknet.version_string()
	 * @since 2025-09-09
	 */
	public String version_short() throws Throwable
	{
		return get_string("darknet_version_short");
	}


	/** Toggle verbose output.  Default verbose setting is @p false.
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 *
	 * @note Turning off verbose output will also turn off trace (if it had been enabled).
	 *
	 * @see @ref Darknet.Darknet.set_trace()
	 * @see @ref Darknet.Darknet.set_output_stream()
	 * @since 2025-09-09
	 */
	public void set_verbose(boolean flag) throws Throwable
	{
		set_bool("darknet_set_verbose", flag);
	}


	/** Toggle trace output.  This is only intended for debugging.  Default trace setting is @p false.
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 *
 	 * @note Turning on trace output will also turn on verbose.  (You canot have trace without also having verbose.)
	 *
	 * @see @ref Darknet.Darknet.set_verbose()
 	 * @see @ref Darknet.Darknet.set_output_stream()
	 * @since 2025-09-09
	 */
	public void set_trace(boolean flag) throws Throwable
	{
		set_bool("darknet_set_trace", flag);
	}


	/** Load the Darknet/YOLO neural network.  Many %Darknet calls require this to have been called, since this sets the
	 * neural network pointer.  Remember to call @ref Darknet.Darknet.free_neural_network() when done.
 	 *
	 * @note Every neural network loaded with @ref Darknet.Darknet.load_neural_network() must be freed with @ref Darknet.Darknet.free_neural_network().
	 *
	 * @since 2025-09-09
	 */
	public MemorySegment load_neural_network(String config, String names, String weights) throws Throwable
	{
		try
		{
			// in case a previous network was loaded, make sure to free everything
			free_neural_network();

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


	/** Free the Darknet/YOLO neural network loaded by @ref Darknet.Darknet.load_neural_network().
	 * It is safe to call this multiple times.
	 *
	 * @note Every neural network loaded with @ref Darknet.Darknet.load_neural_network() must be freed with @ref Darknet.Darknet.free_neural_network().
	 *
	 * @since 2025-09-09
	 */
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


	/** Set the filename into which %Darknet will write console output.  Default is @p STDOUT.
	 * This may be called prior to @ref Darknet.Darknet.load_neural_network().
	 * Use a blank filename ("") to reset it to the default @p STDOUT.
	 *
	 * @since 2025-09-09
	 */
	public void set_output_stream(String output_filename) throws Throwable
	{
		set_string("darknet_set_output_stream", output_filename);
	}


	/** Set the detection threshold.  Default threshold setting is @p 0.25.
	 * @since 2025-09-09
	 */
	public void set_detection_threshold(float threshold) throws Throwable
	{
		set_netptr_float("darknet_set_detection_threshold", threshold);
	}


	/** Set the NMS threshold.  Default NMS setting is @p 0.45.
	 * @since 2025-09-09
	 */
	public void set_non_maximal_suppression_threshold(float threshold) throws Throwable
	{
		set_netptr_float("darknet_set_non_maximal_suppression_threshold", threshold);
	}


	/** Get the network dimensions.  The map will contain 3 entries:  @p width, @p height, and @p channel.
	 * @since 2025-09-09
	 */
	public Map<String, Integer> network_dimensions() throws Throwable
	{
		Map<String, Integer> results = new HashMap<>();

		try
		{
			if (networkptr == null)
			{
				throw new NullPointerException("network pointer is null (neural network is not loaded?), cannot run \"darknet_network_dimensions\"");
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
