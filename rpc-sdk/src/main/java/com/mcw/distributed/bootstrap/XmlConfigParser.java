package com.mcw.distributed.bootstrap;

import com.mcw.distributed.config.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * XML 配置解析器
 */
public class XmlConfigParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlConfigParser.class);

    private static final String NAMESPACE_URI = "http://mcw.com/schema/rpc";

    /**
     * 解析 XML 配置文件
     */
    public static RpcConfig parse(String configFile) {
        try (InputStream input = getConfigInputStream(configFile)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(input);

            return parseDocument(document);

        } catch (Exception e) {
            logger.error("解析XML配置文件失败: {}", configFile, e);
            throw new RuntimeException("解析XML配置文件失败: " + configFile, e);
        }
    }

    private static InputStream getConfigInputStream(String configFile) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(configFile);
        if (input == null) {
            input = XmlConfigParser.class.getClassLoader().getResourceAsStream(configFile);
        }
        if (input == null) {
            throw new RuntimeException("找不到配置文件: " + configFile);
        }
        return input;
    }

    private static RpcConfig parseDocument(Document document) {
        RpcConfig config = new RpcConfig();
        Element root = document.getDocumentElement();

        // 解析注册中心配置
        Element registryElement = getFirstElementByTagName(root, "registry");
        if (registryElement != null) {
            config.setRegistryConfig(parseRegistryConfig(registryElement));
        }

        // 解析服务提供者配置
        Element servicesElement = getFirstElementByTagName(root, "providers");
        if (servicesElement != null) {
            config.setProviderConfigs(parseProviderConfigs(servicesElement));
        }

        // 解析服务消费者配置
        Element referencesElement = getFirstElementByTagName(root, "consumers");
        if (referencesElement != null) {
            config.setConsumerConfigs(parseConsumerConfigs(referencesElement));
        }

        // 解析包扫描配置
        Element scanElement = getFirstElementByTagName(root, "scan");
        if (scanElement != null) {
            config.setScanConfig(parseScanConfig(scanElement));
        }

        logger.info("XML配置解析完成: {} 个服务, {} 个引用",
                config.getProviderConfigs().size(),
                config.getConsumerConfigs().size());

        return config;
    }

    private static RegistryConfig parseRegistryConfig(Element element) {
        RegistryConfig config = new RegistryConfig();
        config.setAddress(element.getAttribute("address"));
        config.setPort(Integer.parseInt(element.getAttribute("port")));
        config.setProtocol(element.getAttribute("protocol"));
        return config;
    }
    

    private static List<ProviderConfig> parseProviderConfigs(Element servicesElement) {
        List<ProviderConfig> configs = new ArrayList<>();
        NodeList serviceNodes = servicesElement.getElementsByTagNameNS(NAMESPACE_URI, "provider");

        for (int i = 0; i < serviceNodes.getLength(); i++) {
            Element serviceElement = (Element) serviceNodes.item(i);
            ProviderConfig config = new ProviderConfig();

            config.setInterfaceName(serviceElement.getAttribute("interface"));
            config.setRef(serviceElement.getAttribute("ref"));
            config.setVersion(serviceElement.getAttribute("version"));

            configs.add(config);
            logger.debug("解析服务配置: {}", config.getInterfaceName());
        }

        return configs;
    }

    private static List<ConsumerConfig> parseConsumerConfigs(Element referencesElement) {
        List<ConsumerConfig> configs = new ArrayList<>();
        NodeList referenceNodes = referencesElement.getElementsByTagNameNS(NAMESPACE_URI, "consumer");

        for (int i = 0; i < referenceNodes.getLength(); i++) {
            Element referenceElement = (Element) referenceNodes.item(i);
            ConsumerConfig config = new ConsumerConfig();

            config.setId(referenceElement.getAttribute("id"));
            config.setInterfaceName(referenceElement.getAttribute("interface"));
            config.setVersion(referenceElement.getAttribute("version"));

            if (referenceElement.hasAttribute("timeout")) {
                config.setTimeout(Integer.parseInt(referenceElement.getAttribute("timeout")));
            }
            if (referenceElement.hasAttribute("retries")) {
                config.setRetries(Integer.parseInt(referenceElement.getAttribute("retries")));
            }

            configs.add(config);
            logger.debug("解析引用配置: {} -> {}", config.getId(), config.getInterfaceName());
        }

        return configs;
    }

    private static ScanConfig parseScanConfig(Element element) {
        ScanConfig config = new ScanConfig();
        config.setBasePackage(element.getAttribute("base-package"));
        return config;
    }

    private static Element getFirstElementByTagName(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagNameNS(NAMESPACE_URI, tagName);
        return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
    }

    public static void main(String[] args) {
        RpcConfig parse = parse("rpc-xml/provider.xml");
        System.out.println(1);
    }
}
