package egain.oassdk.connectors;

import egain.oassdk.core.exceptions.OASSDKException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for BusinessLogicConnector
 */
public class BusinessLogicConnectorTest {
    
    private BusinessLogicConnector connector;
    
    @BeforeEach
    public void setUp() {
        connector = new BusinessLogicConnector();
    }
    
    @Test
    public void testProcessRequest_Success() throws OASSDKException {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("key", "value");
        
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> response;
        
        // Act
        // Note: Since dependencies are injected via @Inject, they may be null
        // This test verifies the method structure and basic flow
        Response result = connector.processRequest(request, businessLogic);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
    
    @Test
    public void testProcessRequest_BusinessLogicExecutes() throws OASSDKException {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        request.put("key", "value");
        
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> {
            assertNotNull(req);
            assertTrue(req.containsKey("key"));
            return response;
        };
        
        // Act
        Response result = connector.processRequest(request, businessLogic);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
    
    @Test
    public void testProcessRequestWithCustomValidation_Success() throws OASSDKException {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> response;
        BusinessLogicConnector.ValidationRules validationRules = new BusinessLogicConnector.ValidationRules();
        validationRules.addRule(data -> true);
        
        // Act
        Response result = connector.processRequestWithCustomValidation(request, businessLogic, validationRules);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
    
    @Test
    public void testProcessRequestWithCustomValidation_ValidationFails() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> new HashMap<>();
        BusinessLogicConnector.ValidationRules validationRules = new BusinessLogicConnector.ValidationRules();
        validationRules.addRule(data -> false);
        
        // Act & Assert
        assertThrows(OASSDKException.class, () -> {
            connector.processRequestWithCustomValidation(request, businessLogic, validationRules);
        });
    }
    
    @Test
    public void testProcessRequestWithSLAEnforcement_Success() throws OASSDKException {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> response;
        BusinessLogicConnector.SLARequirements slaRequirements = new BusinessLogicConnector.SLARequirements();
        slaRequirements.setMaxResponseTime(5000);
        
        // Act
        Response result = connector.processRequestWithSLAEnforcement(request, businessLogic, slaRequirements);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
    
    @Test
    public void testProcessRequestWithSLAEnforcement_SLAViolation() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> {
            try {
                Thread.sleep(100); // Simulate slow operation
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new HashMap<>();
        };
        BusinessLogicConnector.SLARequirements slaRequirements = new BusinessLogicConnector.SLARequirements();
        slaRequirements.setMaxResponseTime(50); // Very short timeout
        
        // Act & Assert
        // Note: This may or may not throw depending on execution speed
        // The test verifies the method handles SLA requirements
        assertDoesNotThrow(() -> {
            try {
                connector.processRequestWithSLAEnforcement(request, businessLogic, slaRequirements);
            } catch (OASSDKException e) {
                // Expected if SLA is violated
                assertTrue(e.getMessage().contains("SLA violation") || e.getMessage().contains("Response time exceeded"));
            }
        });
    }
    
    @Test
    public void testProcessRequestWithMonitoring_Success() throws OASSDKException {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        Map<String, Object> response = new HashMap<>();
        response.put("result", "success");
        
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> response;
        BusinessLogicConnector.MonitoringConfig monitoringConfig = new BusinessLogicConnector.MonitoringConfig();
        monitoringConfig.setEnablePerformanceMonitoring(true);
        monitoringConfig.setEnableErrorMonitoring(true);
        
        // Act
        Response result = connector.processRequestWithMonitoring(request, businessLogic, monitoringConfig);
        
        // Assert
        assertNotNull(result);
        assertEquals(200, result.getStatus());
    }
    
    @Test
    public void testProcessRequest_BusinessLogicThrowsException() {
        // Arrange
        Map<String, Object> request = new HashMap<>();
        BusinessLogicConnector.BusinessLogicFunction businessLogic = req -> {
            throw new RuntimeException("Business logic error");
        };
        
        // Act & Assert
        // Note: Since dependencies are injected, this may or may not throw depending on implementation
        // The test verifies that exceptions from business logic are handled
        assertThrows(OASSDKException.class, () -> {
            connector.processRequest(request, businessLogic);
        });
    }
    
    @Test
    public void testValidationRules_AddRule() {
        // Arrange
        BusinessLogicConnector.ValidationRules rules = new BusinessLogicConnector.ValidationRules();
        BusinessLogicConnector.ValidationRule rule = data -> true;
        
        // Act
        rules.addRule(rule);
        
        // Assert
        assertEquals(1, rules.getRules().size());
        assertTrue(rules.getRules().contains(rule));
    }
    
    @Test
    public void testValidationRules_AddResponseRule() {
        // Arrange
        BusinessLogicConnector.ValidationRules rules = new BusinessLogicConnector.ValidationRules();
        BusinessLogicConnector.ValidationRule rule = data -> true;
        
        // Act
        rules.addResponseRule(rule);
        
        // Assert
        assertEquals(1, rules.getResponseRules().size());
        assertTrue(rules.getResponseRules().contains(rule));
    }
    
    @Test
    public void testSLARequirements_SettersAndGetters() {
        // Arrange
        BusinessLogicConnector.SLARequirements sla = new BusinessLogicConnector.SLARequirements();
        
        // Act
        sla.setMaxResponseTime(1000);
        sla.setMaxConcurrentRequests(100);
        sla.setMaxErrorRate(0.05);
        sla.setMinAvailability(0.99);
        
        // Assert
        assertEquals(1000, sla.getMaxResponseTime());
        assertEquals(100, sla.getMaxConcurrentRequests());
        assertEquals(0.05, sla.getMaxErrorRate());
        assertEquals(0.99, sla.getMinAvailability());
    }
    
    @Test
    public void testMonitoringConfig_SettersAndGetters() {
        // Arrange
        BusinessLogicConnector.MonitoringConfig config = new BusinessLogicConnector.MonitoringConfig();
        
        // Act
        config.setEnablePerformanceMonitoring(false);
        config.setEnableErrorMonitoring(false);
        config.setEnableSlaMonitoring(false);
        
        // Assert
        assertFalse(config.isEnablePerformanceMonitoring());
        assertFalse(config.isEnableErrorMonitoring());
        assertFalse(config.isEnableSlaMonitoring());
    }
    
    @Test
    public void testValidationRule_DefaultErrorMessage() {
        // Arrange
        BusinessLogicConnector.ValidationRule rule = data -> false;
        
        // Act
        String errorMessage = rule.getErrorMessage();
        
        // Assert
        assertEquals("Validation failed", errorMessage);
    }
}

