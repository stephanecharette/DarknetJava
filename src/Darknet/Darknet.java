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

public class Darknet {
	Linker linker;
	SymbolLookup lookup;
	
	public Darknet() 
	{
		// Finds and loads the shared library. Also reserves memory for C functions.
		this.linker = Linker.nativeLinker();
		this.lookup = SymbolLookup.libraryLookup(
			(System.getProperty("os.name").toLowerCase().contains("linux") ? "libdarknet.so" : "darknet.dll"), Arena.global()).
			or(SymbolLookup.loaderLookup()).
			or(Linker.nativeLinker().defaultLookup());
	}

	/** Display version information on @p stdout. Example:
	 *  ````
	 *  Darknet V4 "Slate" v4.0-51-g53faaf9c 
	 *  Darknet is compiled to use the CPU. GPU is disabled.
	 *  OpenCV v4.10.0, Windows 11 Home
	 *  ````
	 *  @see blah
	 */
	public void show_version_info() {
		try (Arena arena = Arena.ofConfined())
		{
			MemorySegment function = lookup.findOrThrow("darknet_show_version_info"); // Finds the function in C code
			FunctionDescriptor descriptor = FunctionDescriptor.ofVoid(); // Gives a return type and parameters to the function
			MethodHandle handle = linker.downcallHandle(function, descriptor); // creates a "handle" 
			handle.invokeExact();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public String darknet_version_string()
	{
		try (Arena arena = Arena.ofConfined())
		{
			MemorySegment function = lookup.findOrThrow("darknet_version_string"); // Finds the function in C code
			FunctionDescriptor descriptor = FunctionDescriptor.of(ValueLayout.ADDRESS); // Gives a return type and parameters to the function
			MethodHandle handle = linker.downcallHandle(function, descriptor); // creates a "handle" 
			
			MemorySegment CString = (MemorySegment) handle.invokeExact();
//			String result = CString.getU; 
			return null;
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return "Error";
		}
	}
	
}
