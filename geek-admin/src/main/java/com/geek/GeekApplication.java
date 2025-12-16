package com.geek;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import lombok.extern.log4j.Log4j2;

/**
 * 启动程序
 * 
 * @author geek
 */
@EnableCaching
@EnableMethodSecurity
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@MapperScan(basePackages = "com.geek.**.mapper")
@Log4j2
public class GeekApplication {
    public static void main(String[] args) throws UnknownHostException {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        ConfigurableApplicationContext application = SpringApplication.run(GeekApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  极客启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'     " + " ____           _         " + "\n" +
                " |  |\\ \\  |  ||   |(_,_)'    " + "  / ___| ___  ___| | __   " + "\n" +
                " |  | \\ `'   /|   `-'  /      " + "| |  _ / _ \\/ _ \\ |/ /  " + "\n" +
                " |  |  \\    /  \\      /      " + " | |_| |  __/  __/   <    " + "\n" +
                " ''-'   `'-'    `-..-'         " + "\\____|\\___|\\___|_|\\_\\");

        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        System.out.println("\n----------------------------------------------------------\n" +
                " Application geek-Geek is running! Access URLs:\n" +
                " Local:        http://localhost:" + port + "/\n" +
                " External:     http://" + ip + ":" + port + "/\n" +
                " Swagger文档:  http://" + ip + ":" + port + "/swagger-ui/index.html\n" +
                " Knife4j文档:  http://" + ip + ":" + port + "/doc.html" + "" + "\n" +
                "----------------------------------------------------------");
    }
}
