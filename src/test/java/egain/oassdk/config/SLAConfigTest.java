package egain.oassdk.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

/**
 * Comprehensive test cases for SLAConfig
 */
public class SLAConfigTest {
    
    @Test
    public void testDefaultConstructor() {
        SLAConfig config = new SLAConfig();
        
        assertNull(config.getSlaFile());
        assertTrue(config.isMonitoring());
        assertTrue(config.isAlerting());
        assertNotNull(config.getCompliance());
        assertTrue(config.getCompliance().contains("gdpr"));
        assertTrue(config.getCompliance().contains("iso27001"));
        assertEquals("aws", config.getApiGateway());
        assertNotNull(config.getMonitoringStack());
        assertTrue(config.getMonitoringStack().contains("prometheus"));
        assertTrue(config.getMonitoringStack().contains("grafana"));
        assertNotNull(config.getAdditionalProperties());
    }
    
    @Test
    public void testConstructorWithParameters() {
        List<String> compliance = Arrays.asList("gdpr", "hipaa");
        List<String> monitoringStack = Arrays.asList("datadog", "newrelic");
        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("key", "value");
        
        SLAConfig config = new SLAConfig(
            "sla.yaml", false, false, compliance, "azure", monitoringStack, additionalProps
        );
        
        assertEquals("sla.yaml", config.getSlaFile());
        assertFalse(config.isMonitoring());
        assertFalse(config.isAlerting());
        assertEquals(compliance, config.getCompliance());
        assertEquals("azure", config.getApiGateway());
        assertEquals(monitoringStack, config.getMonitoringStack());
        assertEquals("value", config.getAdditionalProperties().get("key"));
    }
    
    @Test
    public void testGettersAndSetters() {
        SLAConfig config = new SLAConfig();
        
        config.setSlaFile("test-sla.yaml");
        assertEquals("test-sla.yaml", config.getSlaFile());
        
        config.setMonitoring(false);
        assertFalse(config.isMonitoring());
        
        config.setAlerting(false);
        assertFalse(config.isAlerting());
        
        List<String> compliance = Arrays.asList("gdpr");
        config.setCompliance(compliance);
        assertEquals(compliance, config.getCompliance());
        
        config.setApiGateway("gcp");
        assertEquals("gcp", config.getApiGateway());
        
        List<String> stack = Arrays.asList("prometheus");
        config.setMonitoringStack(stack);
        assertEquals(stack, config.getMonitoringStack());
        
        Map<String, Object> props = new HashMap<>();
        props.put("test", "value");
        config.setAdditionalProperties(props);
        assertEquals(props, config.getAdditionalProperties());
    }
    
    @Test
    public void testBuilder() {
        SLAConfig config = SLAConfig.builder()
            .slaFile("sla.yaml")
            .monitoring(false)
            .alerting(false)
            .compliance(Arrays.asList("gdpr"))
            .apiGateway("azure")
            .monitoringStack(Arrays.asList("datadog"))
            .build();
        
        assertEquals("sla.yaml", config.getSlaFile());
        assertFalse(config.isMonitoring());
        assertFalse(config.isAlerting());
        assertEquals(1, config.getCompliance().size());
        assertEquals("azure", config.getApiGateway());
        assertEquals(1, config.getMonitoringStack().size());
    }
    
    @Test
    public void testBuilderWithAdditionalProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("key1", "value1");
        props.put("key2", 123);
        
        SLAConfig config = SLAConfig.builder()
            .additionalProperties(props)
            .build();
        
        assertEquals("value1", config.getAdditionalProperties().get("key1"));
        assertEquals(123, config.getAdditionalProperties().get("key2"));
    }
    
    @Test
    public void testBuilderChaining() {
        SLAConfig config = SLAConfig.builder()
            .slaFile("test.yaml")
            .monitoring(true)
            .alerting(true)
            .compliance(Arrays.asList("gdpr", "iso27001"))
            .apiGateway("aws")
            .monitoringStack(Arrays.asList("prometheus", "grafana"))
            .build();
        
        assertNotNull(config);
        assertEquals("test.yaml", config.getSlaFile());
        assertEquals("aws", config.getApiGateway());
    }
    
    @Test
    public void testToString() {
        SLAConfig config = new SLAConfig();
        String str = config.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("SLAConfig"));
        assertTrue(str.contains("monitoring"));
    }
    
    @Test
    public void testNullAdditionalProperties() {
        SLAConfig config = new SLAConfig(
            "sla.yaml", true, true, null, "aws", null, null
        );
        
        assertNotNull(config.getCompliance());
        assertNotNull(config.getMonitoringStack());
        assertNotNull(config.getAdditionalProperties());
    }
}

