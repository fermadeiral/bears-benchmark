package io.swagger.codegen.java;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.AbstractJavaCodegen;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;

public class AbstractJavaCodegenTest {

    private final AbstractJavaCodegen fakeJavaCodegen = new P_AbstractJavaCodegen();

    @Test
    public void toEnumVarNameShouldNotShortenUnderScore() throws Exception {
        Assert.assertEquals("UNDERSCORE", fakeJavaCodegen.toEnumVarName("_", "String"));
        Assert.assertEquals("__", fakeJavaCodegen.toEnumVarName("__", "String"));
        Assert.assertEquals("__", fakeJavaCodegen.toEnumVarName("_,.", "String"));
    }

    @Test
    public void toVarNameShouldAvoidOverloadingGetClassMethod() throws Exception {
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("class"));
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("_class"));
        Assert.assertEquals("propertyClass", fakeJavaCodegen.toVarName("__class"));
    }

    @Test
    public void toModelNameShouldUseProvidedMapping() throws Exception {
        fakeJavaCodegen.importMapping().put("json_myclass", "com.test.MyClass");
        Assert.assertEquals("com.test.MyClass", fakeJavaCodegen.toModelName("json_myclass"));
    }

    @Test
    public void toModelNameUsesPascalCase() throws Exception {
        Assert.assertEquals("JsonAnotherclass", fakeJavaCodegen.toModelName("json_anotherclass"));
    }

    @Test
    public void preprocessSwaggerWithFormParamsSetsContentType() {
        Path dummyPath = new Path()
                .post(new Operation().parameter(new FormParameter()))
                .get(new Operation());

        Swagger swagger = new Swagger()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessSwagger(swagger);

        Assert.assertNull(swagger.getPath("dummy").getGet().getVendorExtensions().get("x-contentType"));
        Assert.assertEquals(swagger.getPath("dummy").getPost().getVendorExtensions().get("x-contentType"), "application/x-www-form-urlencoded");
    }

    @Test
    public void preprocessSwaggerWithBodyParamsSetsContentType() {
        Path dummyPath = new Path()
                .post(new Operation().parameter(new BodyParameter()))
                .get(new Operation());

        Swagger swagger = new Swagger()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessSwagger(swagger);

        Assert.assertNull(swagger.getPath("dummy").getGet().getVendorExtensions().get("x-contentType"));
        Assert.assertEquals(swagger.getPath("dummy").getPost().getVendorExtensions().get("x-contentType"), "application/json");
    }

    @Test
    public void preprocessSwaggerWithNoFormOrBodyParamsDoesNotSetContentType() {
        Path dummyPath = new Path()
                .post(new Operation())
                .get(new Operation());
        
        Swagger swagger = new Swagger()
                .path("dummy", dummyPath);

        fakeJavaCodegen.preprocessSwagger(swagger);

        Assert.assertNull(swagger.getPath("dummy").getGet().getVendorExtensions().get("x-contentType"));
        Assert.assertNull(swagger.getPath("dummy").getPost().getVendorExtensions().get("x-contentType"));
    }

    @Test
    public void testInitialConfigValues() throws Exception {
        final AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), null);
    }

    @Test
    public void testAdditionalPropertiesPutForConfigValues() throws Exception {
        final AbstractJavaCodegen codegen = new P_AbstractJavaCodegen();
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

    private static class P_AbstractJavaCodegen extends AbstractJavaCodegen {
        @Override
        public CodegenType getTag() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getHelp() {
            return null;
        }
    }
}
