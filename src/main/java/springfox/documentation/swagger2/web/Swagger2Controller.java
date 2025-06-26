//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package springfox.documentation.swagger2.web;

import com.datalight.tools.deserialization.service.impl.RedisDataServiceImpl;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponents;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.PropertySourcedMapping;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger.common.HostNameProvider;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@ApiIgnore
public class Swagger2Controller {
    public static final String DEFAULT_URL = "/v2/api-docs";
    private static final Logger LOGGER = LoggerFactory.getLogger(Swagger2Controller.class);
    private static final String HAL_MEDIA_TYPE = "application/hal+json";
    private final String hostNameOverride;
    private final DocumentationCache documentationCache;
    private final ServiceModelToSwagger2Mapper mapper;
    private final JsonSerializer jsonSerializer;

    @Autowired
    public Swagger2Controller(Environment environment, DocumentationCache documentationCache, ServiceModelToSwagger2Mapper mapper, JsonSerializer jsonSerializer) {
        this.hostNameOverride = environment.getProperty("springfox.documentation.swagger.v2.host", "DEFAULT");
        this.documentationCache = documentationCache;
        this.mapper = mapper;
        this.jsonSerializer = jsonSerializer;
    }

    @RequestMapping(
            value = {"/v2/api-docs"},
            method = {RequestMethod.GET},
            produces = {"application/json", "application/hal+json"}
    )
    @PropertySourcedMapping(
            value = "${springfox.documentation.swagger.v2.path}",
            propertyKey = "springfox.documentation.swagger.v2.path"
    )
    @ResponseBody
    public ResponseEntity<Json> getDocumentation(@RequestParam(value = "group",required = false) String swaggerGroup, HttpServletRequest servletRequest) {
        String groupName = (String)Optional.fromNullable(swaggerGroup).or("default");
        Documentation documentation = this.documentationCache.documentationByGroup(groupName);
        if (documentation == null) {
            LOGGER.warn("Unable to find specification for group {}", groupName);
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } else {
            Swagger swagger = this.mapper.mapDocumentation(documentation);
            UriComponents uriComponents = HostNameProvider.componentsFrom(servletRequest, swagger.getBasePath());
            swagger.basePath(Strings.isNullOrEmpty(uriComponents.getPath()) ? "/" : uriComponents.getPath());
            try{
                QueryParameter p = (QueryParameter)((Path)swagger.getPaths().get("/searchByEnv")).getGet()
                        .getParameters().stream().filter(x -> "envName".equals(x.getName())).findFirst().get();
                String path =  RedisDataServiceImpl.acquirePath();
                List<String> items = Files.readAllLines(Paths.get(path, new String[0]))
                        .stream().filter(x -> {return x != null && x.contains("#");}).map(x -> x.split("#")[0]).collect(Collectors.toList());
                p.setEnum(items);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            if (Strings.isNullOrEmpty(swagger.getHost())) {
                swagger.host(this.hostName(uriComponents));
            }

            return new ResponseEntity(this.jsonSerializer.toJson(swagger), HttpStatus.OK);
        }
    }

    private String hostName(UriComponents uriComponents) {
        if ("DEFAULT".equals(this.hostNameOverride)) {
            String host = uriComponents.getHost();
            int port = uriComponents.getPort();
            return port > -1 ? String.format("%s:%d", host, port) : host;
        } else {
            return this.hostNameOverride;
        }
    }
}
