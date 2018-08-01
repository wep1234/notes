
public class DownLoadTest {
	public static void main(String[] args) {
		 String filepath = "需要下载的地址";
	        MultiTheradDownLoad load = new MultiTheradDownLoad(filepath ,4);    
	        load.downloadPart();    
	}
}
