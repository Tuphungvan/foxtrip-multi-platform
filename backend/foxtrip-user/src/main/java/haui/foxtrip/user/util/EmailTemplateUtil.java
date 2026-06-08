package haui.foxtrip.user.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

@UtilityClass
public class EmailTemplateUtil {

    public static String render(String templatePath, Map<String, String> variables) {
        String template = read(templatePath);
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() == null ? "" : entry.getValue();
            template = template.replace(placeholder, value);
        }
        return template;
    }
    private static String read(String templatePath) {
        ClassPathResource resource = new ClassPathResource(templatePath);
        try (InputStream input = resource.getInputStream()) {
            return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to load email template: " + templatePath, ex);
        }
    }
}
