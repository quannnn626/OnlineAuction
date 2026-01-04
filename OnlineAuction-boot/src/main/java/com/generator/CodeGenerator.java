package com.generator;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CodeGenerator {

    //数据库连接参数    ##常修改
    public static String driver = "com.mysql.cj.jdbc.Driver";
    public static String url = "jdbc:mysql://localhost:3307/onlineauction?characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&rewriteBatchedStatements=true";
    public static String username = "root";
    public static String password = "root";

    //父级别报名称
    public static String parentPackage = "com.auction.onlineauction";
    //代码生成的目标路径
    public static String generateTo = "/src/main/java";
    //mapper.xml生成的目标路径
    public static String mapperXmlPath = "/src/main/resources/mapper";
    //控制器 公共基类
    public static String baseControllerClassName;

    //业务层公共基类
    public static String baseServiceClassName;
    //作者
    public static String author = "MrYan";
    //模块名称
    public static String modelName = "OnlineAuction";
    //mapper接口模版文件,不用写后缀 .ftl
    // 设置为 null 使用默认模板，或使用自定义模板路径（如：/templates/mapper.java）
    public static String mapperTemplate = null;

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotBlank(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + generateTo);
        gc.setAuthor(author);
        gc.setOpen(false);
        // gc.setSwagger2(true); 实体属性 Swagger2 注解
        //时间类型
        gc.setDateType(DateType.TIME_PACK);
        //设置mapper.xml的ResultMap
        gc.setBaseResultMap(true);

        mpg.setGlobalConfig(gc);

        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(url);
        // dsc.setSchemaName("public");
        dsc.setDriverName(driver);
        dsc.setUsername(username);
        dsc.setPassword(password);
        mpg.setDataSource(dsc);

        // 包配置
        PackageConfig pc = new PackageConfig();
//        pc.setModuleName(scanner("模块名"));
        pc.setModuleName(modelName);
        pc.setParent(parentPackage);
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };

        // 如果模板引擎是 freemarker
//        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        // String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig("/templates/mapper.xml.ftl") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + mapperXmlPath
                        + "/" + tableInfo.getEntityName() + "Mapper" + StringPool.DOT_XML;
            }
        });

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);
        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml(null); // 禁用默认的 XML 生成，使用自定义配置
        if (mapperTemplate != null) {
            templateConfig.setMapper(mapperTemplate);
        }
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel);
        //驼峰命名开启
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //设置实体类使用lombok
        strategy.setEntityLombokModel(true);
        //设置controller的父类
        if (baseControllerClassName != null) strategy.setSuperControllerClass(baseControllerClassName);
        if (baseServiceClassName != null) strategy.setSuperServiceImplClass(baseServiceClassName);
//        strategy.setSuperEntityClass("你自己的父类实体,没有就不用设置!");
//        strategy.setEntityLombokModel(true);
//        strategy.setRestControllerStyle(true);
//        // 公共父类
//        strategy.setSuperControllerClass("你自己的父类控制器,没有就不用设置!");
//        // 写于父类中的公共字段
//        strategy.setSuperEntityColumns("id");
//        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));

        //设置实体类属性对应字段的注解
        strategy.setEntityTableFieldAnnotationEnable(true);
        //设置表名
        String tableName = scanner("表名,all全部");
        if (!"all".equalsIgnoreCase(tableName)){
            strategy.setInclude(tableName);
        }

//        strategy.setControllerMappingHyphenStyle(true);
        strategy.setTablePrefix(pc.getModuleName() + "_");
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);
        //选择FreeMarker  引擎
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
    }


}
