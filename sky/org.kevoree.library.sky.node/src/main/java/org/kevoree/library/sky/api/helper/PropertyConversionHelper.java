package org.kevoree.library.sky.api.helper;

/**
 * User: Erwan Daubert - erwan.daubert@gmail.com
 * Date: 30/07/12
 * Time: 10:13
 *
 * @author Erwan Daubert
 * @version 1.0
 */

public class PropertyConversionHelper {

  public static Long getRAM (String ram) throws NumberFormatException {
    if (ram.toLowerCase().endsWith("gb")) {
        return java.lang.Long.parseLong(ram.substring(0, ram.length() - 2)) * 1024 * 1024 * 1024;
    } else if (ram.toLowerCase().endsWith("mb")) {
        return java.lang.Long.parseLong(ram.substring(0, ram.length() - 2)) * 1024 * 1024;
    } else if (ram.toLowerCase().endsWith("kb")) {
        return java.lang.Long.parseLong(ram.substring(0, ram.length() - 2)) * 1024 * 1024;
    } else {
        return java.lang.Long.parseLong(ram);
    }
  }

  public static Long getCPUFrequency (String cpu)throws NumberFormatException {
    if (cpu.toLowerCase().endsWith("ghz")) {
        return java.lang.Long.parseLong(cpu.substring(0, cpu.length() - 3)) * 1024 * 1024 * 1024;
    } else if (cpu.toLowerCase().endsWith("mhz")) {
        return java.lang.Long.parseLong(cpu.substring(0, cpu.length() - 3)) * 1024 * 1024;
    } else if (cpu.toLowerCase().endsWith("khz")) {
        return java.lang.Long.parseLong(cpu.substring(0, cpu.length() - 3)) * 1024 * 1024;
    } else {
        return java.lang.Long.parseLong(cpu);
    }
  }

//  @throws(classOf[NumberFormatException])
public static Long getDataSize (String dataSize) throws NumberFormatException {
    if (dataSize.toLowerCase().endsWith("gb")) {
      return java.lang.Long.parseLong(dataSize.substring(0, dataSize.length() - 2)) * 1024 * 1024 * 1024;
    } else if (dataSize.toLowerCase().endsWith("mb")) {
        return java.lang.Long.parseLong(dataSize.substring(0, dataSize.length() - 2)) * 1024 * 1024;
    } else if (dataSize.toLowerCase().endsWith("kb")) {
        return java.lang.Long.parseLong(dataSize.substring(0, dataSize.length() - 2)) * 1024 * 1024;
    } else {
        return java.lang.Long.parseLong(dataSize);
    }
  }

}
