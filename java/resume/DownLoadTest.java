
public class DownLoadTest {
	public static void main(String[] args) {
		 String filepath = "��Ҫ���صĵ�ַ";
	        MultiTheradDownLoad load = new MultiTheradDownLoad(filepath ,4);    
	        load.downloadPart();    
	}
}
