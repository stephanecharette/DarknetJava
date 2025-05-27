package Darknet;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

public class Darknet
{
	Linker linker;
	SymbolLookup lookup;

	/// NETWORKPTR is a Darknet::NetworkPtr, which is an opaque C-style void pointer to a structure internal to Darknet.
	public static final AddressLayout NETWORKPTR = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE));

	/// C_POINTER is a generic C-style pointer.
	public static final AddressLayout C_POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE));

	public Darknet()
	{
		// Finds and loads the shared library. Also reserves memory for C functions.
		this.linker = Linker.nativeLinker();
		this.lookup = SymbolLookup.libraryLookup(
			(System.getProperty("os.name").toLowerCase().contains("linux") ? "libdarknet.so" : "darknet.dll"), Arena.global()).
			or(SymbolLookup.loaderLookup()).
			or(Linker.nativeLinker().defaultLookup());
	}

	/// Run a function that takes no parameters and returns nothing (void).
	private void run_v(String name)
	{
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid());
			handle.invokeExact();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return;
	}

	/// Run a function that takes no parameters and returns a text string.
	private String run_s(String name)
	{
		MemorySegment result = null;
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.of(ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, ValueLayout.JAVA_BYTE))));
			result = (MemorySegment) handle.invokeExact();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return result.getString(0);
	}

	/// Run a function that takes a boolean and returns nothing.
	private void run_b(String name, boolean flag)
	{
		try
		{
			MemorySegment function = lookup.findOrThrow(name);
			MethodHandle handle = linker.downcallHandle(function, FunctionDescriptor.ofVoid(ValueLayout.JAVA_BOOLEAN));
			handle.invokeExact(flag);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return;
	}

	/// Run a function that takes a string and returns nothing.
	private void run_str(String name, String str)
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
			e.printStackTrace();
		}
		return;
	}

	public MemorySegment string_to_memorysegment(String str)
	{
		MemorySegment seg = Arena.global().allocateFrom(str, java.nio.charset.StandardCharsets.UTF_8);
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
	public void show_version_info()
	{
		run_v("darknet_show_version_info");
	}

	public String version_string()
	{
		return run_s("darknet_version_string");
	}

	public String version_short()
	{
		return run_s("darknet_version_short");
	}

	public void set_verbose(boolean flag)
	{
		run_b("darknet_set_verbose", flag);
	}

	public void set_trace(boolean flag)
	{
		run_b("darknet_set_trace", flag);
	}

	public MemorySegment load_neural_network(String config, String names, String weights)
	{
		MemorySegment ptr = null;
		try
		{
			MemorySegment fn1 = string_to_memorysegment(config);
			MemorySegment fn2 = string_to_memorysegment(names);
			MemorySegment fn3 = string_to_memorysegment(weights);

			MemorySegment function = lookup.findOrThrow("darknet_load_neural_network");
			FunctionDescriptor descriptor = FunctionDescriptor.of(NETWORKPTR, C_POINTER, C_POINTER, C_POINTER);
			MethodHandle handle = linker.downcallHandle(function, descriptor);
			ptr = (MemorySegment) handle.invokeExact(fn1, fn2, fn3);

			System.out.format("neural network loaded .. %s%n", ptr);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		return ptr;
	}

	public void free_neural_network(MemorySegment ptr)
	{
		if (ptr != null)
		{
			try
			{
				System.out.format("free network pointer ... %s%n", ptr);

				MemorySegment function = lookup.findOrThrow("darknet_free_neural_network");
				FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(NETWORKPTR);
				MethodHandle handle = linker.downcallHandle(function, descriptor);
				handle.invokeExact(ptr);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public void set_output_stream(String output_filename)
	{
		run_str("darknet_set_output_stream", output_filename);
	}
}
