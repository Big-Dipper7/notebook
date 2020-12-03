package Hou.Evenodd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;


public class EncodeDecode extends SplitMerge{
	/**
	 * Evenodd+码的编码
	 * @throws IOException 
	 * */
	public  byte[][][] encode(String filePath) throws IOException {
		EncodeDecode encode = new EncodeDecode();
		//读取已经分割的文件块
		File file = new File(filePath);
		byte[][] tempMatrix = new byte[K][(int) DISK_SIZE];
		for (int i = 0; i < K; i++) {
			tempMatrix[i] = encode.FileToBlock(file.getParent() + "\\2--" + i + ".tmp");
		}
		
		byte[][][] tempMemory = encode.BlockToMatrix(tempMatrix);
//		System.out.println("原始数据：");
//		display(tempMemory[0]);
		
		byte[][][] dataCache = new byte[count][M - 1][K];
		for (int i = 0; i < count; i++) {
			dataCache[i] = encode.getColumnData(tempMemory[i]);
		}
//		System.out.println("转置后数据：");
//		display(dataCache[0]);
		

		byte[][] tempMatrix1 = new byte[count][M - 1];//行校验
		byte[][] tempMatrix2 = new byte[count][M - 1];//对角线校验
		byte[][][] temp = new byte[count][M - 1][K + 2];
		byte s[] = new byte[count];//计算公共因子
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			s[i] = encode.getCommonFactor(dataCache[i]);
			tempMatrix1[i] = encode.horiXOR(dataCache[i]);
			tempMatrix2[i] = encode.diagXOR(dataCache[i], s[i]);
			temp[i] = encode.lastMatrix(tempMatrix1[i], tempMatrix2[i], dataCache[i]);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("EVENODD+严格编码时间：" + (endTime - startTime) + "ms");
		
//		System.out.println("公共因子为：" + s[0]);
		System.out.println("存储矩阵为：" );
		display(temp[0]);
	
		//转化为文件形式存储
		encode.BlockToFile(encode.MatrixToBlock(temp,K),K);
		encode.BlockToFile(encode.MatrixToBlock(temp,K + 1),K + 1);

		return temp;
	}
	
	/**
	 * 解码
	 * @throws IOException 
	 * */
	public  byte[][][] decode(int error1, int error2, String filePath) throws IOException{
		EvenoddHou decode = new EvenoddHou();
		/**
		 * 两磁盘出错
		 * */
		if(error1 != -1 && error2 != -1) {
			
			/**
			 * 两校验盘出错 解码相当于编码
			 * */
			if(error1 == K && error2 == K + 1) {
				encode(filePath);
			}
			
			/**
			 * 一数据盘和对角线校验盘出错
			 * */
			if(error1 >= 0 && error1 < K && error2 == K + 1) {
				byte[][] tempMatrix = new byte[K + 2][(int) DISK_SIZE];
				for (int i = 0; i < K + 2; i++) {
					if(i == error1 || i == error2) {} //补零
					else 
						tempMatrix[i] = FileToBlock((new File(filePath)).getParent()+ "\\2--" + i + ".tmp");
				}
				
				//转换为矩阵
				byte[][][] tempMemory = BlockToMatrix(tempMatrix);
//				System.out.println("输出源数据：");
//				display(tempMemory[0]);
				
				//转置矩阵
				byte[][][] dataCache = new byte[count][M - 1][K + 2];
				for (int i = 0; i < count; i++) {
					dataCache[i] = getColumnData(tempMemory[i]);
				}
				System.out.println("输出转置后数据：");
				display(dataCache[0]);

				//修复数据盘
				for (int c = 0; c < count; c++) {
					for (int i = 0; i < M - 1; i++) {
						for(int j = 0; j < K + 1; j++) {
							if(j != error1) {
								dataCache[c][i][error1] = (byte)(dataCache[c][i][error1] ^ dataCache[c][i][j]);
							}
						}
					}
				}
				
//				System.out.println("修复第一列后：");
//				display(dataCache[2]);
				
				//恢复error2 对角线校验盘
				byte[] s=new byte[count];
				byte[][] tempMatrix2=new byte[count][];
				for(int c = 0;c<count;c++)
				{
					s[c]=decode.getCommonFactor(dataCache[c]);
					tempMatrix2[c]=decode.diagXOR(dataCache[c],s[c]);
					for(int i=0;i<tempMatrix2[c].length;i++)
				    {
				    	dataCache[c][i][error2]=tempMatrix2[c][i];
				    }
				}
				//输出
				 System.out.println("修复后第二列后：");
				 display(dataCache[0]);
				
				 BlockToFile(MatrixToBlock(dataCache,error1),error1);
				 BlockToFile(MatrixToBlock(dataCache,error2),error2);
				 return dataCache;
				
			}
			
			
			/**
			 * 一数据盘和行校验盘出错
			 * */
			if(error1 >= 0 && error1 < K && error2 == K) {
				byte[][] tempMatrix = new byte[K + 2][(int) DISK_SIZE];
				for (int i = 0; i < K + 2; i++) {
					if(i == error1 || i == error2) {} //补零
					
					else tempMatrix[i] = FileToBlock((new File(filePath)).getParent()+ "\\2--" + i + ".tmp");
				}
				
				//转换为矩阵
				byte[][][] tempMemory = BlockToMatrix(tempMatrix);
//				System.out.println("输出源数据：");
//				display(tempMemory[16748]);
				
				//转置矩阵
				byte[][][] dataCache = new byte[count][M - 1][K + 2];
				for (int i = 0; i < count; i++) {
					dataCache[i] = getColumnData(tempMemory[i]);
				}
//				System.out.println("输出转置后数据：");
//				display(dataCache[62584]);
				
				long startTime = System.currentTimeMillis();
				
				byte s[] = new byte[count];//计算公共因子
				byte[][] tempMatrix3 = new byte[count][M - 1];//对角线校验
				for (int c = 0; c < count; c++) {
					for (int i = 0; i < M - 1; i++) {
						if(i < (2 * Math.floor(K / 2))) {
							if(error1 == 0) {
								s[c] = decode.getCommonFactor(dataCache[c]);
								tempMatrix3[c] = decode.diagXOR(dataCache[c], s[c]);
							    dataCache[c][i][error1] = (byte) (dataCache[c][i][K + 1] ^ tempMatrix3[c][i]);	
							}else {
								byte temp = dataCache[c][error1 - 1][K + 1];
								for (int j1 = 0; j1 < K; j1++) {
									if(j1 != error1) {
										temp = (byte) (temp ^ dataCache[c][getMod(error1 - 1 - j1, M)][j1]);
									}
								}
								for (int j2 = 1; j2 < K; j2++) {
									if(j2 != error1) {
										temp = (byte) (temp ^ dataCache[c][getMod(M - 1 - j2, M)][j2]);
									}
								}
								dataCache[c][M - error1 - 1][error1] = temp;
								s[c] = decode.getCommonFactor(dataCache[c]);
								byte temp1 = 0;
								temp1 = (byte) (s[c] ^ dataCache[c][i][K + 1]);
								for (int j = 0; j < K; j++) {
									if(j != error1) {
										int t = getMod(i - j, M);
										if(t != M -1) {
											temp1 = (byte) (temp1 ^ dataCache[c][t][j]);
										}
										
									}
								}
								int t = getMod(i - error1, M);
								if(t != M - 1) {
									dataCache[c][t][error1] = temp1;
								}
								
							}
						}else {
							dataCache[c][i - error1][error1] ^= dataCache[c][i][K + 1];
							for (int j = 0; j < K; j++) {
								if(j != error1) {
									dataCache[c][i - error1][error1] = (byte) (dataCache[c][i - error1][error1] ^ dataCache[c][getMod(i - j, M)][j]);
								}
							}
						}
						
					}
				}
				
				//恢复error2 第一个校验盘
				byte[][] tempMatrix1 = new byte[count][];
				for (int i = 0; i < count; i++) {
					tempMatrix1[i] = decode.horiXOR(dataCache[i]);
					for (int j = 0; j < M - 1; j++) {
						dataCache[i][j][error2] = tempMatrix1[i][j];
					}
				}
				long endTime = System.currentTimeMillis();
				System.out.println("f+P情况的严格解码时间：" + (endTime-startTime) + "ms");
				//用文件格式输出
				BlockToFile(MatrixToBlock(dataCache, error1), error1);
				BlockToFile(MatrixToBlock(dataCache, error2), error2);
				
				System.out.println("修复后的数据：");
				display(dataCache[16748]);
				return dataCache;
				
			}
			
			
			/**
			 * 两数据盘出错              
			 * */
			if(error1 >= 0 && error1 < K && error2 >= 0 && error2 < K) {
				byte[][] tempMatrix = new byte[K + 2][(int) DISK_SIZE];
				for (int i = 0; i < K + 2; i++) {
					if(i == error1 || i == error2) {} //补零
					else tempMatrix[i] = FileToBlock((new File(filePath)).getParent()+ "\\2--" + i + ".tmp");
				}

				//转换为矩阵
				byte[][][] tempMemory = BlockToMatrix(tempMatrix);
//				System.out.println("输出源数据：");
//				display(tempMemory[0]);
				
				//转置矩阵
				byte[][][] dataCache = new byte[count][M - 1][K + 2];
				for (int i = 0; i < count; i++) {
					dataCache[i] = getColumnData(tempMemory[i]);
				}
//				System.out.println("输出转置后数据：");
//				display(dataCache[0]);
				
				
				//增加一个元素全为0的行
				byte[][][] temp = new byte[count][M][K + 2];
				for(int c=0;c<count;c++)
				{
					temp[c]=addRow(dataCache[c],temp[c]);
				}
//				display(temp[0]);
				long startTime = System.currentTimeMillis();
				byte s[] = new byte[count];//计算公共因子
				for (int c = 0; c < count; c++) {
					for (int i = 0; i < M - 1; i++) {
						s[c] =  (byte) (s[c] ^ temp[c][i][K] ^ temp[c][i][K + 1]);
						
					}
				}
				System.out.println("公共因子是：" + s[0]);
				
				byte[][] tempkp1 = new byte[count][M]; //行校验列
				byte[][] tempkp2 = new byte[count][M]; //对角线校验列
				for (int c = 0; c < count; c++) {
					for (int i = 0; i < M; i++) {
						tempkp1[c][i] = (byte) (temp[c][i][K]);
						if(i < (2 * Math.floor(K / 2))) {
							tempkp2[c][i] =  (byte) (temp[c][i][K + 1] ^ s[c]);
						}else {
							tempkp2[c][i] =  (byte) (temp[c][i][K + 1]);
						}

						for (int j = 0; j < K; j++) {
							if(j != error1 && j != error2) {
								tempkp1[c][i] ^= temp[c][i][j];//行校验列的剩余元素
								int t = getMod(i - j, M);
								if(t != M - 1) {
									tempkp2[c][i] = (byte)(tempkp2[c][i] ^ temp[c][t][j]);//对角线校验列的剩余元素
								}else if(error1 != 0){
									tempkp2[c][i] = s[c];
								}
									
								
							}
						}
					}
				
					
					for (int k = 1; k < M; k++) {

						int t = getMod(M - 1 + k * (error2 - error1), M);
						int t1 = getMod(M - 1 + (k - 1) * (error2 - error1), M);
						int t2 = getMod(M - 1 + (k + 1) * (error2 - error1), M);
						int tw = 0;
						if(error1 == 0) {
							tw = t;
						}else {
							tw = t2;
						}
						if(k == 1) {
							temp[c][t][error1] = (byte) (tempkp2[c][tw] ^ temp[c][M - 1][error2]);//
							temp[c][t][error2] = (byte) (temp[c][t][error1] ^ tempkp1[c][t]);
						}else {
							temp[c][t][error1] = (byte) (temp[c][t1][error2] ^ tempkp2[c][tw]);//
							temp[c][t][error2] = (byte) (temp[c][t][error1] ^ tempkp1[c][t]);
						}
					}
					//映射到源数据矩阵中
					for (int i = 0; i < M - 1; i++) {
						dataCache[c][i][error1] = temp[c][i][error1];
						dataCache[c][i][error2] = temp[c][i][error2];
					}
				}
				long endTime = System.currentTimeMillis();
				System.out.println("严格解码时间：" + (endTime-startTime) + "ms");
//				System.out.println("修复完毕后："); 
//				display(dataCache[0]);
				
				BlockToFile(MatrixToBlock(dataCache,error1),error1);
				BlockToFile(MatrixToBlock(dataCache,error2),error2);
				
				return dataCache;
			}
		}
		
		return null;
	}
	
	public  String getFileMD5(File file) { //检测修复前后文件是否一致
	    if (!file.isFile()) {
	        return null;
	    }
	    MessageDigest digest = null;
	    FileInputStream in = null;
	    byte buffer[] = new byte[8192];
	    int len;
	    try {
	        digest =MessageDigest.getInstance("MD5");
	        in = new FileInputStream(file);
	        while ((len = in.read(buffer)) != -1) {
	            digest.update(buffer, 0, len);
	        }
	        BigInteger bigInt = new BigInteger(1, digest.digest());
	        return bigInt.toString(16);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    } finally {
	        try {
	            in.close();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	  
	}

		public static boolean equal(byte[][] arr1, byte[][] arr2) {
	 
			if (arr1 == null) {
	 
				return (arr2 == null);
	 
			}
	 
			if (arr2 == null) {
	 
				return false;
	 
			}
	 
			if (arr1.length != arr2.length) {
	 
				return false;
	 
			}
	 
			for (int i = 0; i < arr1.length; i++) {
				if (!Arrays.equals(arr1[i], arr2[i])) {
						return false;
				}
			}
			return true;
		}
	
		public static void MatrixDiff(byte[][][] dataCache1, byte[][][] dataCache2) {
			for (int c = 0; c < count; c++) {
				for (int i = 0; i < K - 1; i++) {
					for (int j = 0; j < K + 2; j++) {
						if(dataCache1[c][i][j] != dataCache2[c][i][j]) {
							System.out.println("第" + c + "个矩阵出问题！");
							break;
						}
					}
				}
			}
		} 
		
		
	
	public static void main(String[] args) throws IOException {

				//获取文件块长度
				String filePath = "F:\\test\\1.jpg";
				EncodeDecode file1 = new EncodeDecode();
				length=(int) file1.getFileLength(filePath);
				if((length % K) != 0) {
					DISK_SIZE = (length / K) + 1;//取上限
				}else
					DISK_SIZE = length / K;
				
				if((DISK_SIZE % (M - 1)) != 0)
					count = (int)(DISK_SIZE / (M - 1) + 1);
				else
					count = (int)(DISK_SIZE / (M - 1));
				
				DISK_SIZE += ((M-1)* count - DISK_SIZE);
				
			    System.out.println(length);//40875
			    System.out.println(DISK_SIZE);//8175
			    System.out.println();
			    
			    file1.split(filePath);
			   
//			    long enStartTime = System.currentTimeMillis();
//			    file1.encode(filePath);
//			    long enEndTime = System.currentTimeMillis();
//				System.out.println("编码时间：" + (enEndTime - enStartTime) + "ms");
				
				long deStartTime = System.currentTimeMillis();
				file1.decode(1, 3, filePath);	   
			    long deEndTime = System.currentTimeMillis();
				System.out.println("解码时间：" + (deEndTime - deStartTime) + "ms");
				
			    file1.merge(filePath);
	}
	
	
}
