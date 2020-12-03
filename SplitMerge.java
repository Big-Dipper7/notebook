package Hou.Evenodd;

import java.io.*;
import java.nio.channels.FileChannel;

public class SplitMerge extends EvenoddHou{

	public static long DISK_SIZE;//一个文件碎块的大小(一个条带？？)
	public static int length = 0;
	public static int count = 0; //块的大小
	
	/**
	 * 对文件进行分块
	 * */
	public void split(String filePath) throws IOException {
		int blockNo;
		File file = new File(filePath);
		byte[] readBuffer = new byte[(int) DISK_SIZE];//一次性读取DISK_SIZE
		
		DataInputStream fpr = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
		blockNo = 0;
		try {	
			while(true) {
				DataOutputStream fpw = new DataOutputStream(new FileOutputStream(file.getParent() + "\\2--" + blockNo + ".tmp"));
				fpr.read(readBuffer);
				fpw.write(readBuffer);
				fpw.flush();
				fpw.close();
				blockNo++;
				if(blockNo >= K) break;
			}
		}catch(EOFException e) {	
			fpr.close();
		}
		System.out.println("文件分割完成");
	}
	
	/**
	 * 对文件进行组合
	 * */
	public void merge(String filePath) throws IOException {
		int blockNo;
		byte[] writerBuffer = new byte[(int) DISK_SIZE];
		
//		//去除补充的字节数
		int differ = (int) (DISK_SIZE * K - length);
		System.out.println("differ=" + differ);
		byte[] writerBufferDiffer = new byte[(int)(DISK_SIZE - differ)];
		int dot = filePath.lastIndexOf('.');
		DataOutputStream fpw = new DataOutputStream(new FileOutputStream(new File(filePath).getParent() + "\\3." + filePath.substring(dot + 1)));
		blockNo = 0;
		try {
			for (int i = 0; i < K; i++) {
				DataInputStream fpr = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(filePath).getParent() + "\\2--"+ blockNo + ".tmp")));
				if(i != K - 1) {
					fpr.read(writerBuffer);
					fpw.write(writerBuffer);
				}else {
					fpr.read(writerBufferDiffer);
					fpw.write(writerBufferDiffer);
				}
				
				
				fpr.close();
				blockNo++;
			}
			//最后几块特殊处理
//				DataInputStream fpr=new DataInputStream(new BufferedInputStream(new FileInputStream("./2-"+blockNo+".jpg")));
//		    	fpr.read(writerBufferDiffer);
//		    	fpw.write(writerBufferDiffer);
//		    	fpw.flush();
//		    	fpr.close();
//		    	blockNo++;	
		} catch (IOException e) {
			fpw.close();
		}
		
		System.out.println("文件合并完成！");
		}
	
	/**
	 * 将文件转换为分块
	 * @throws IOException 
	 * */
	public byte[] FileToBlock(String filePath) throws IOException {
		
		byte[] fileBuffer = new byte[(int) DISK_SIZE];
		DataInputStream fpr = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
		try {
			while(true) {
				fpr.read(fileBuffer);
				if(fileBuffer.length == DISK_SIZE) break; //不手动退出的话，java相关网络协议一直不关闭，一直在循环
			}			
		}catch(EOFException e) {
			fpr.close();
		}
		return fileBuffer;
	}
	
	/******
	 * 将分块转换为文件
	 * @return
	 * @throws IOException 
	 * 
	 */
	public  void BlockToFile(byte[] tempMatrix,int blockNo ) throws IOException
	{
		DataOutputStream fpw=new DataOutputStream(new FileOutputStream("F:\\test\\2--"+blockNo+".tmp"));
		fpw.write(tempMatrix);
		fpw.flush();
		fpw.close();	
	}

	
	/******
	 * 求文件的长度
	 * @return
	 * 
	 */	
	public long getFileLength(String filePath)
	{
		long ret = 0;
		FileChannel fc=null;
		try{
			File f=new File(filePath);
			if(f.exists() && f.isFile())
			{
				FileInputStream fis=new FileInputStream(f);
				fc=fis.getChannel();
				ret=fc.size();
				fis.close();
			}
			else
			{
				System.out.print("file dones't exit or is not a file");
			}
		}catch(FileNotFoundException e)
		{
			
		}
		catch(IOException e){
			
		}
		finally{
			if(null!=fc)
			{
				try{
					fc.close();
				}catch(IOException e)
				{
					
				}
			}
		}
		return ret;
	}	
	
	/******
	 * 将分块转换为矩阵
	 * @return
	 * @throws IOException 
	 * 
	 */
	public byte[][][] BlockToMatrix(byte[][] tempMatrix)
	{
		int m=tempMatrix.length;
		
//		//矩阵的个数
//		if((DISK_SIZE % (M - 1)) != 0)
//			count = (int)(DISK_SIZE / (M - 1) + 1);
//		else
//			count = (int)(DISK_SIZE / (M - 1));
//		System.out.println("count:" + count);
		byte[][][] tempMemory=new byte[count][m][M - 1];//将数据划分为矩阵
	    int t = 0;
		for(int c = 0; c < count; c++)
		{
//			if(c == count - 1) {
//				DISK_SIZE = DISK_SIZE + 3;
//				
//			}
		    for(int i = 0; i < m; i++)
		    {
		    	t = (M - 1) * c;
		    	for(int j = 0; j < M - 1; j++)
		    	{
		    		//对最后的一个矩阵补齐行数
		    		if(t >= DISK_SIZE)
		    		{
		    			tempMemory[c][i][j] = 0;
		    		}
		    		else
		    			tempMemory[c][i][j] = tempMatrix[i][t];
		    		t++;
		    	}	
	    		
			}
		 }

		return tempMemory;
	}

	/******
	 * 将矩阵转换为 块
	 * @param m 
	 * @return
	 * @throws IOException 
	 * 
	 */
	public byte[] MatrixToBlock(byte[][][] dataCache, int m) 
	{
		byte[] temp = new byte[(int) DISK_SIZE];
		int k = 0;
		int j = m;
		for(int c = 0; c < count; c++)
		{
			for(int i = 0; i < M - 1; i++)
			{
				if(k >= DISK_SIZE)
					break;
				else
					temp[k] = dataCache[c][i][j];
				k++;
			}
			
			
		}
		return temp;
	}
}