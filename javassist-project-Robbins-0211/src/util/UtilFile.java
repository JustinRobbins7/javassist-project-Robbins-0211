package util;

import java.io.File;

public class UtilFile {
   static String _S_ = File.separator;
   
   public static String getShortFileName(String file) {
      int lastIdx = file.lastIndexOf(_S_);
      String shortFileName = file.substring(lastIdx);
      return shortFileName;
   }
}
