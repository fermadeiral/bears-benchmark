package io.swagger.codegen.lua;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.languages.LuaClientCodegen;

public class LuaClientCodegenTest {

    @Test
    public void testInitialConfigValues() throws Exception {
        final LuaClientCodegen codegen = new LuaClientCodegen();
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), "true");
    }

    @Test
    public void testAdditionalPropertiesPutForConfigValues() throws Exception {
        final LuaClientCodegen codegen = new LuaClientCodegen();
        codegen.additionalProperties().put(CodegenConstants.HIDE_GENERATION_TIMESTAMP, false);
        codegen.processOpts();

        Assert.assertEquals(codegen.additionalProperties().get(CodegenConstants.HIDE_GENERATION_TIMESTAMP), Boolean.FALSE);
    }

}
