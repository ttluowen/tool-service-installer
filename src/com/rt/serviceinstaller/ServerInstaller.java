package com.rt.serviceinstaller;

/**
 * 服务安装器执行入口类。
 * 
 * @since 2016-01-20
 * @version 1.0
 * @author Luowen
 */
public class ServerInstaller {

	/**
	 * 执行入口。
	 * 
	 * @param args install|uninstall。
	 */
	public static void main(String[] args) {
		
		if (args != null && args.length > 0) {
			String type = args[0].toLowerCase();

			if (type.equals("install")) {
				// 执行安装操作。
				new ServiceBase().install();
			} else if (type.equals("uninstall")) {
				// 执行安装操作。
				new ServiceBase().uninstall();
			}
		} else {
			System.out.println("参数不正确，请传递 install 或 uninstall 参数");
		}
	}
}
