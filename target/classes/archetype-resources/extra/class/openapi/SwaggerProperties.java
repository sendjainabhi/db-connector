#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package}.config.properties;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import lombok.Getter;
import lombok.Setter;

@Data
@Configuration
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    
    private Map<String, URLS> extensions;
    
    private String projectTitle;
    private String projectDescription;
    private String projectVersion;
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> ext(){
    	return (Map)this.extensions;
    }
    
    @Getter
    @Setter
    public static class URLS extends Object{
        private String[] urls;        
    }
}
