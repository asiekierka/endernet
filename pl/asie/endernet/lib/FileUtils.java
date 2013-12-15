package pl.asie.endernet.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import net.minecraftforge.common.DimensionManager;
import pl.asie.endernet.EnderNet;

import com.google.gson.Gson;

public class FileUtils {
	public static String load(File file) {
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), "UTF-8")
					);
			String s = "";
			while(true) {
				String s1 = reader.readLine();
				if(s1 == null) break;
				s += s1;
			}
			reader.close();
			return s;
		} catch(Exception e) { e.printStackTrace(); return null; }
	}
	
	public static void save(String s, File file) {
		try {
			PrintWriter out = new PrintWriter(file);
			out.println(s);
			out.flush();
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
