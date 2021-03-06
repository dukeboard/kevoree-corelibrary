/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kevoree.library.javase.voldemort.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jed
 * Date: 03/10/12
 * Time: 17:21
 * To change this template use File | Settings | File Templates.
 */
public class FileManager {


    public static byte[] load(InputStream reader) throws IOException {
        int c;
        ArrayList<Byte> tab = new ArrayList<Byte>();
        while((c = reader.read()) != -1) {
            tab.add((byte)c);
        }
        if (reader!=null)
            reader.close();
        return toByteArray(tab);
    }

    public static String copyFileFromStream( InputStream inputStream , String path, String targetName,boolean replace) throws IOException {

        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            copy.mkdirs();
            copy.deleteOnExit();
            if(replace)
            {
                if(copy.exists()){
                    if(!copy.delete()){
                       throw new IOException("delete file "+copy.getPath());
                    }
                    if(!copy.createNewFile()){
                        throw new IOException("createNewFile file "+copy.getPath());
                    }
                }
            }
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);

            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return copy.getAbsolutePath();
        }
        return null;
    }
    public static byte[] toByteArray(List<Byte> in) {
        final int n = in.size();
        byte ret[] = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }

    public  static byte[] load(String pathfile)
    {
        File file = new File(pathfile);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        ArrayList<Byte> tab = new ArrayList<Byte>();
        try
        {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            while (dis.available() != 0)
            {
                tab.add((byte)dis.read());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                fis.close();
                bis.close();
                dis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return  toByteArray(tab);
    }


    /* Utility fonctions */
    public static void deleteOldFile(File folder) {
        if (folder.isDirectory()) {
            for (File f : folder.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteOldFile(f);
                }
            }
        }
        folder.delete();
    }


    public static void writeFile(String path,String data,Boolean append) throws IOException
    {
        File file = new File(path.substring(0,path.lastIndexOf("/")));
        file.mkdirs();

        FileWriter fileWriter = new FileWriter(path,append);
        BufferedWriter out_j = new BufferedWriter(fileWriter);
        out_j.write(data);
        out_j.close();
    }
    public static String copyFileFromPath(String inputFile, String path, String targetName) throws IOException {
        InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream(inputFile);
        if (inputStream != null) {
            File copy = new File(path + File.separator + targetName);
            //copy.deleteOnExit();
            copy.setExecutable(true);
            OutputStream outputStream = new FileOutputStream(copy);
            byte[] bytes = new byte[1024];
            int length = inputStream.read(bytes);

            while (length > -1) {
                outputStream.write(bytes, 0, length);
                length = inputStream.read(bytes);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();

            return copy.getAbsolutePath();
        }
        return null;
    }

    public static void display_message_process(final InputStream data){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String line;
                BufferedReader input =  new BufferedReader(new InputStreamReader(data));
                try
                {
                    while ((line = input.readLine()) != null)
                        System.out.println(line);
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        });
        t.start();

    }


    public  static  boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++){
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                    return false;
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static  void copyDirectory(File srcPath, File dstPath)
            throws IOException{

        if (srcPath.isDirectory()){

            if (!dstPath.exists()){

                dstPath.mkdir();

            }


            String files[] = srcPath.list();

            for(int i = 0; i < files.length; i++){
                copyDirectory(new File(srcPath, files[i]),
                        new File(dstPath, files[i]));

            }

        }

        else{

            if(!srcPath.exists()){

                System.out.println("File or directory does not exist.");

                System.exit(0);

            }

            else

            {

                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];

                int len;

                while ((len = in.read(buf)) > 0) {

                    out.write(buf, 0, len);

                }

                in.close();

                out.close();

            }

        }
    }

}
