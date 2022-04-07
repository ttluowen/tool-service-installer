package com.rt.serviceinstaller;

import com.rt.log.Logger;
import com.rt.util.number.NumberUtil;
import com.rt.util.proterty.PropertyUtil;
import com.rt.util.string.StringUtil;
import com.rt.web.config.SystemConfig;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;


/**
 * 系统服务的安装与卸载管理类。
 *
 * @author Luowen
 * @version 1.0
 * @since 2016-01-20
 */
public class ServiceBase {

    /**
     * Java 的最低版本要求，如果低于该版本的将不能使用。
     */
    private static final float MIN_JAVA_VERSION = 1.7f;
    /**
     * config.properties 配置文件位置。
     */
    private static final String CONFIG_FILE = "config.properties";

    // 记录当前是否初始化过。
    private static boolean inited = false;

    // 配置文件的属性。
    private Properties config;


    /**
     * 构造函数。
     */
    public ServiceBase() {

        init();
    }


    /**
     * 初始化。
     */
    private void init() {

        // 检测是否初始化过了。
        if (inited) {
            return;
        }


        // 标记已初始化过。
        inited = true;


        // 获取系统运行所在路径。
        String systemPath = getSystemPath();


        // 设置系统配置管理类的系统目录。
        SystemConfig.setSystemPath(systemPath);
        ;


        // 设置日志的系统目录。
        Logger.setSystemPath(getRootPath());

        // 打开启动日志。
        Logger.log("启动成功，运行环境" + systemPath);
    }


    /**
     * 安装服务操作。
     */
    public void install() {

        // 验证 Java 版本号。
        String javaVersion = getJavaVersion();
        if (checkJavaVersion(javaVersion)) {
            Logger.log("Java版本验证通过，您当前的版本是" + javaVersion);
        } else {
            Logger.log("对不起，Java版本不得低于" + MIN_JAVA_VERSION + "，您当前的版本是" + javaVersion);
            return;
        }


        // 先执行卸载操作。
        uninstall();


        // 创建安装的命令行。
        String installCommand = buildInstallCommand();
        if (!StringUtil.isEmpty(installCommand)) {
            executeCommand(installCommand);
        } else {
            Logger.log("创建添加服务的命令行失败");
            return;
        }
    }


    /**
     * 卸载服务操作。
     */
    public void uninstall() {

        // 创建卸载的命令行。
        String uninstallCommand = buildUninstallCommand();
        if (!StringUtil.isEmpty(uninstallCommand)) {
            executeCommand(uninstallCommand);
        } else {
            Logger.log("创建卸载服务的命令行失败");
            return;
        }
    }


    /**
     * 获取系统运行所在的位置。
     *
     * @return
     */
    private String getSystemPath() {

        // 获取当前系统运行环境的磁盘路径，并统一将 / 目录转换成 \ 方式。
        String path = System.getProperty("user.dir");

        // 将路径等转换成当前系统格式，并在最后自动补足目录符号。
        path = SystemConfig.formatFilePath(path);


        return path;
    }


    /**
     * 获取当前 java 版本号。
     * 1.7.0_45
     * 1.8.0_60
     *
     * @return
     */
    private String getJavaVersion() {

        return System.getProperty("java.version");
    }


    /**
     * 检测 Java 的版本号。
     *
     * @return
     */
    private boolean checkJavaVersion(String version) {

        // 再根据版本号来比较是否满足最低要求。。
        return getVersion(version) >= MIN_JAVA_VERSION;
    }


    /**
     * 将字符字符串的版本号转换成浮点数型的。
     * <p>
     * 1.7.0_45 ==> 1.7
     * 1.8.0_60 ==> 1.8
     *
     * @param version
     * @return
     */
    private float getVersion(String version) {

        // 验证参数有效性。
        if (StringUtil.isEmpty(version)) {
            return 0f;
        }


        // 分割出各段版本号。
        String[] splitedVersion = version.split("\\.");


        /*
         * 取最前面的大版本号，转换成浮点数。
         * 通过浮点数的方式比较版本号，只取前两级版本号标识。
         * 1.0
         */
        float versionFloat = NumberUtil.parseFloat(splitedVersion[0]);

        /*
         * 添加第二段版本号。
         * 最终形成一个浮点数的版本标识。
         * 1.7  ==> 1.0 + 0.7
         * 1.8  ==> 1.0 + 0.8
         */
        if (splitedVersion.length > 1) {
            versionFloat += NumberUtil.parseFloat("0." + splitedVersion[1]);
        }


        return versionFloat;
    }


    /**
     * 读取配置文件。
     *
     * @return
     */
    private Properties getConfigProp() {

        if (config != null) {
            return config;
        } else {
            return config = PropertyUtil.read(SystemConfig.getSystemPath() + CONFIG_FILE);
        }
    }


    /**
     * 获取 config 文件的配置项。
     *
     * @param name
     * @return
     */
    private String getConfigValue(String name) {

        String value = StringUtil.unNull(getConfigProp().getProperty(name));


        // 读取 properties 属性时，默认是 ISO-8859-1 编码的，所以转换成 UTF-8。
        try {
            value = new String(value.getBytes(StringUtil.ISO_8859_1), StringUtil.UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return value;
    }


    /**
     * 获取 jar 的配置内容。
     *
     * @return
     */
    private String getConfigOfJar() {

        return getConfigValue("jar");
    }


    /**
     * 获取 serviceName 的配置内容。
     *
     * @return
     */
    private String getConfigOfServiceName() {

        return getConfigValue("serviceName");
    }


    /**
     * 获取 serviceDescription 的配置内容。
     *
     * @return
     */
    private String getConfigOfServiceDescription() {

        return getConfigValue("serviceDescription");
    }


    /**
     * 获取 startup 的配置内容。
     *
     * @return
     */
    private String getConfigOfStartup() {

        return getConfigValue("startup");
    }


    /**
     * 获取停止服务时调用的类名。
     *
     * @return
     */
    private String getConfigOfStopClass() {

        return getConfigValue("stopClass");
    }


    /**
     * 获取初始内存大小，单位 MB，如果参数无效则使用默认值 32。
     *
     * @return
     */
    private int getConfigOfXms() {

        int value = NumberUtil.parseInt(getConfigValue("xms"));

        if (value == 0) {
            value = 32;
        }


        return value;
    }


    /**
     * 获取最大内存大小，单位 MB，如果参数无效则使用默认值 2048。
     *
     * @return
     */
    private int getConfigOfXmx() {

        int value = NumberUtil.parseInt(getConfigValue("xmx"));

        if (value == 0) {
            value = 2048;
        }


        return value;
    }


    /**
     * 获取 log 日志配置路径。
     *
     * @return
     */
    private String getConfigOfRoot() {

        return getConfigValue("root");
    }


    /**
     * 获取 jar 包里的 main 类。
     *
     * @return
     */
    private String getMainClassName() {

        String className = null;

        try {
            // 获取 jar 的配置项。
            String jar = SystemConfig.transformFileSeparator(SystemConfig.getSystemPath() + getConfigOfJar());

            if (!jar.isEmpty()) {
                // 加载 jar 文件。
                URL jarUrl = new URL("jar:file:" + jar + "!/");
                // 转换成 jar 加载连接器。
                JarURLConnection conn = (JarURLConnection) jarUrl.openConnection();

                if (conn != null) {
                    // 获取 main 属性。
                    Attributes attr = conn.getMainAttributes();

                    if (attr != null) {
                        // 获取 Rsrc-Main-Class 指向的类。
                        className = attr.getValue("Rsrc-Main-Class");

                        // 如果 Rsrc-Main-Class 没有值，再尝试使用 Main-Class。
                        if (StringUtil.isEmpty(className)) {
                            className = attr.getValue(Attributes.Name.MAIN_CLASS);
                        }
                    } else {
                        Logger.log("没有可用的 main 属性。");
                    }
                } else {
                    Logger.log("加载 jar 文件失败。");
                }
            } else {
                Logger.log("没有可用的 jar 配置项。");
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }


        return className;
    }


    /**
     * 获取当前操作系统的位数。
     * 32、64
     *
     * @return
     */
    private int getOsBit() {

        // 系统架构信息。
        String osArch = System.getProperty("os.arch");
        // 系统位数。
        int osBit = osArch.indexOf("64") != -1 ? 64 : 32;


        return osBit;
    }


    /**
     * 获取当前 JVM 的位数。
     *
     * @return
     */
    private int getJvmBit() {

        // Java 虚拟机名称。
        String jvmName = System.getProperty("java.vm.name");
        // JVM 位数。
        int jvmBit = jvmName.indexOf("64") != -1 ? 64 : 32;


        return jvmBit;
    }


    /**
     * 获取 JavaService.exe 文件的路径。
     * 不同的操作系统位数，会使用不同的文件。
     *
     * @return
     */
    private String getJavaServicePath() {

        String javaService = SystemConfig.getSystemPath() + "JavaService-" + getOsBit() + "bit.exe";


        return javaService;
    }


    /**
     * 获取服务日志打开位置。
     *
     * @return
     */
    private String getRootPath() {

        // 安装和运行时的位置。
        // CURRENT、PARENT、其它完整的路径。
        String root = getConfigOfRoot();
        String systemPath = SystemConfig.getSystemPath();


        if (!StringUtil.isEmpty(root)) {
            if (root.equals("CURRENT")) {
                root = systemPath;
            } else if (root.equals("PARENT")) {
                // 查询最后一级目录的索引位置。
                String fileSeparator = SystemConfig.FILE_SEPARATOR;
                String systemPathCopy = systemPath.substring(0, systemPath.length() - 1);
                int lastFilePathIndex = systemPathCopy.lastIndexOf(fileSeparator);

                // 去掉最后一级目录，并保留目录符号。
                root = systemPath.substring(0, lastFilePathIndex + 1);
            } else {
                root = systemPath;
            }
        }


        return root;
    }


    /**
     * 创建安装服务的命令行。
     *
     * @return
     */
    private String buildInstallCommand() {

        // 获取系统运行时路径。
        String systemPath = SystemConfig.getSystemPath();

        // 系统位数。
        int osBit = getOsBit();
        // JVM 位数。
        int jvmBit = getJvmBit();

        // Java 安装运行目录。
        String javaHome = System.getProperty("java.home");
        // Java 目录的 jvm.dll 文件路径。
        String jvmPath = SystemConfig.transformFileSeparator(javaHome + "\\bin\\server\\jvm.dll");


        // 要安装使用的服务名。
        String serviceName = getConfigOfServiceName();
        // 服务描述。
        String serviceDescription = getConfigOfServiceDescription();
        // 服务运行类型。
        String startup = getConfigOfStartup();
        // 引用的 JavaService.exe 位置及文件。
        String javaServicePath = getJavaServicePath();
        // 获取要注册的 Jar 包文件路径。
        String jarPath = getConfigOfJar();
        // 配置的 Jar 包文件执行的 main 类。
        String mainClass = getMainClassName();

        // 停止服务时调用的类名。
        String stopClass = getConfigOfStopClass();

        // 获取初始化内存大小。
        int xms = getConfigOfXms();
        // 获取最大内存大小。
        int xmx = getConfigOfXmx();

        // 运行的 jar 包文件。
        String runJarPath = SystemConfig.transformFileSeparator(systemPath + jarPath);
        // 安装和运行服务时的位置。
        String rootPath = getRootPath();
        // 服务日志输出的位置。
        String serviceLogsPath = rootPath + "logs\\";
        // 服务普通日志。
        String serviceOutPath = serviceLogsPath + "service-out.log";
        // 服务错误日志。
        String serviceErrorPath = serviceLogsPath + "service-error.log";
        // 服务运行时的环境位置，这个路径分割符必须使用(/)的。
        String currentPath = rootPath.replace("\\", "/");


        // 检测系统与Java虚拟机是否是相同位数的。
        if (osBit != jvmBit) {
            Logger.log("系统与Java虚拟机位数不致，OS：" + osBit + "，Java：" + jvmBit);

            return null;
        }

        // 检测 Jar 包中是否有可用的 main 执行类。
        if (StringUtil.isEmpty(mainClass)) {
            Logger.log("没有可用的 main 执行类。");

            return null;
        }

        // 检测要安装的服务名是否可用。
        if (serviceName.isEmpty()) {
            Logger.log("服务名称不可用");

            return null;
        }


        // 检测日志文件夹是否存在，不存在提前创建。
        File serviceLogsDir = new File(serviceLogsPath);
        if (!serviceLogsDir.exists()) {
            serviceLogsDir.mkdirs();
        }


        StringBuffer command = new StringBuffer()
                // JavaService.exe 文件。
                .append("\"").append(javaServicePath).append("\" ")
                // 安装的服务名称。
                .append("-install ").append(serviceName).append(" ")
                // JVM 路径。
                .append("\"").append(jvmPath).append("\" ")
                // JVM 运行内存配置，初始化内存、最大内存。
                .append("-Xms").append(xms).append("M -Xmx").append(xmx).append("M ")
                // 要运行的 Jar 包路径。
                .append("-Djava.class.path=\"").append(runJarPath).append("\" ")
                // 启动的 main 类。
                .append("-start ").append(mainClass).append(" ")
                // 服务停止时调用的类。
                .append(
                        stopClass.isEmpty()
                                ? ""
                                : "-stop " + stopClass + " "
                )
                // 服务日志输出位置。
                .append("-out \"").append(serviceOutPath).append("\" ")
                // 服务错误输出位置。
                .append("-err \"").append(serviceErrorPath).append("\" ")
                // 服务运行时的环境位置。
                .append("-current \"").append(currentPath).append("\" ")
                // 服务启动类型。
                .append("-").append(startup).append(" ")
                // 服务描述。
                .append("-description \"").append(serviceDescription).append("\"")

                // 换行，添加到下一条执行命令。
                .append("\n")

                // 启动服务。
                .append("NET START ").append(serviceName);


        return command.toString();
    }


    /**
     * 创建卸载的命令行。
     *
     * @return
     */
    private String buildUninstallCommand() {

        // 获取使用的服务名称。
        String serviceName = getConfigOfServiceName();


        // 检测配置的服务名称是否有效。
        if (serviceName.isEmpty()) {
            Logger.log("服务名称不可用");

            return null;
        }


        // 设置删除服务的命令行。
        StringBuffer command = new StringBuffer()
                .append("net stop ").append(serviceName).append("\n")
                .append("sc delete ").append(serviceName);


        return command.toString();
    }


    /**
     * 执行安装服务的命令行。
     *
     * @param command
     */
    private void executeCommand(String command) {

        List<Process> processes = new ArrayList<Process>();

        try {
            // 执行 CMD 命令，并返回它的进程。
            String[] commands = command.split("\n");
            for (String cmd : commands) {
                Logger.log("执行命令：" + cmd);

                // 执行。
                Process process = Runtime.getRuntime().exec(cmd);

                // 添加当前进程至队列，用于最后关闭。
                processes.add(process);


                // 使用一般的去监视命令执行时正常打印的信息。
                commandMonitor(process.getInputStream());
                // 单独用一个线程去监视命令执行时打印的错误信息。
                commandMonitor(process.getErrorStream());
            }


            Logger.log("命令执行完成");
        } catch (Exception e) {
            Logger.printStackTrace(e);
        } finally {
            // 销毁进程。
            for (Process process : processes) {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }


    /**
     * 监视命令执行时打印的流信息。
     *
     * @param in
     */
    private void commandMonitor(final InputStream in) {

        // 使用线程去执行。
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StringUtil.UTF8));

            // 读取相关的内容。
            String line;
            while ((line = reader.readLine()) != null) {
                Logger.log(line);
            }


            // 读取结束，关闭。
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            Logger.printStackTrace(e);
        }
    }
}
