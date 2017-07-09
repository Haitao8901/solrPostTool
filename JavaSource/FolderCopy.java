
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FolderCopy {
	public static void main(String[] args){
		String from = "D:\\apache-tomcat-7.0.54";
		String to = "C:\\targetsrc";
		copyFolder(from, to);
	}
	
	private static void copyFolder(String from, String to){
		File toFile = new File(to);
		if(!toFile.exists()){
			toFile.mkdirs();
		}
		
		File fromFile  = new File(from);
		if(fromFile.isDirectory()){
			File[] files = fromFile.listFiles();
			for(File file:files){
				if(file.isDirectory()){
					copyFolder(file.getAbsolutePath(), to+File.separator + file.getName());
				}else{
					String fileName = file.getName();
					copyFile(file, toFile.getAbsolutePath() + File.separator + fileName);
				}
			}
		}else{
			String targetPath = toFile.getAbsolutePath() + File.separator + fromFile.getName();
			copyFile(fromFile, targetPath);
		}
	}
	
	private static void copyFile(File file, String targetPath){
		try {
			FileOutputStream fo = new FileOutputStream(targetPath);
			FileInputStream fi = new FileInputStream(file);
			int byteRead;
			byte[] bytes = new byte[1024];
			while((byteRead = fi.read(bytes)) != -1){
				fo.write(bytes, 0, byteRead);
			}
			fi.close();
			fo.flush();
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void copyFolder(){
		
	}
}
