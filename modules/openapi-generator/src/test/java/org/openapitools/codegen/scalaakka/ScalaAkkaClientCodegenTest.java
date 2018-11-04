/*
 * Copyright 2018 OpenAPI-Generator Contributors (https://openapi-generator.tech)
 * Copyright 2018 SmartBear Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openapitools.codegen.scalaakka;

import com.google.common.collect.Sets;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.parser.util.SchemaTypeUtil;

import org.openapitools.codegen.CodegenModel;
import org.openapitools.codegen.CodegenProperty;
import org.openapitools.codegen.DefaultCodegen;
import org.openapitools.codegen.languages.ScalaAkkaClientCodegen;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class ScalaAkkaClientCodegenTest {

    private ScalaAkkaClientCodegen scalaAkkaClientCodegen;

    @Test(description = "convert a simple scala model")
    public void simpleModelTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("name", new StringSchema())
                .addProperties("createdAt", new DateTimeSchema())
                .addRequiredItem("id")
                .addRequiredItem("name");
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 3);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "id");
        Assert.assertEquals(property1.getter, "getId");
        Assert.assertEquals(property1.setter, "setId");
        Assert.assertEquals(property1.dataType, "Long");
        Assert.assertEquals(property1.name, "id");
        Assert.assertNull(property1.defaultValue);
        Assert.assertEquals(property1.baseType, "Long");
        Assert.assertTrue(property1.hasMore);
        Assert.assertTrue(property1.required);
        Assert.assertTrue(property1.isNotContainer);

        final CodegenProperty property2 = cm.vars.get(1);
        Assert.assertEquals(property2.baseName, "name");
        Assert.assertEquals(property2.getter, "getName");
        Assert.assertEquals(property2.setter, "setName");
        Assert.assertEquals(property2.dataType, "String");
        Assert.assertEquals(property2.name, "name");
        Assert.assertNull(property2.defaultValue);
        Assert.assertEquals(property2.baseType, "String");
        Assert.assertTrue(property2.hasMore);
        Assert.assertTrue(property2.required);
        Assert.assertTrue(property2.isNotContainer);

        final CodegenProperty property3 = cm.vars.get(2);
        Assert.assertEquals(property3.baseName, "createdAt");
        Assert.assertEquals(property3.getter, "getCreatedAt");
        Assert.assertEquals(property3.setter, "setCreatedAt");
        Assert.assertEquals(property3.dataType, "DateTime");
        Assert.assertEquals(property3.name, "createdAt");
        Assert.assertNull(property3.defaultValue);
        Assert.assertEquals(property3.baseType, "DateTime");
        Assert.assertFalse(property3.hasMore);
        Assert.assertFalse(property3.required);
        Assert.assertTrue(property3.isNotContainer);
    }

    @Test(description = "convert a model with list property")
    public void listPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("id", new IntegerSchema().format(SchemaTypeUtil.INTEGER64_FORMAT))
                .addProperties("urls", new ArraySchema()
                        .items(new StringSchema()))
                .addRequiredItem("id");
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        //Assert.assertEquals(cm.vars.size(), 2);

        final CodegenProperty property1 = cm.vars.get(1);
        Assert.assertEquals(property1.baseName, "urls");
        Assert.assertEquals(property1.getter, "getUrls");
        Assert.assertEquals(property1.setter, "setUrls");
        Assert.assertEquals(property1.dataType, "Seq[String]");
        Assert.assertEquals(property1.name, "urls");
        Assert.assertEquals(property1.defaultValue, "Seq[String].empty ");
        Assert.assertEquals(property1.baseType, "Seq");
        Assert.assertEquals(property1.containerType, "array");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(property1.isContainer);
    }

    @Test(description = "convert a model with a map property")
    public void mapPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("translations", new MapSchema()
                        .additionalProperties(new StringSchema()))
                .addRequiredItem("id");
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "translations");
        Assert.assertEquals(property1.getter, "getTranslations");
        Assert.assertEquals(property1.setter, "setTranslations");
        Assert.assertEquals(property1.dataType, "Map[String, String]");
        Assert.assertEquals(property1.name, "translations");
        Assert.assertEquals(property1.defaultValue, "Map[String, String].empty ");
        Assert.assertEquals(property1.baseType, "Map");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(property1.isContainer);
    }

    @Test(description = "convert a model with complex properties")
    public void complexPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("children", new Schema().$ref("#/definitions/Children"));
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.getter, "getChildren");
        Assert.assertEquals(property1.setter, "setChildren");
        Assert.assertEquals(property1.dataType, "Children");
        Assert.assertEquals(property1.name, "children");
        Assert.assertNull(property1.defaultValue);
        Assert.assertEquals(property1.baseType, "Children");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(property1.isNotContainer);
    }

    @Test(description = "convert a model with complex list property")
    public void complexListPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("children", new ArraySchema()
                        .items(new Schema().$ref("#/definitions/Children")));
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.complexType, "Children");
        Assert.assertEquals(property1.getter, "getChildren");
        Assert.assertEquals(property1.setter, "setChildren");
        Assert.assertEquals(property1.dataType, "Seq[Children]");
        Assert.assertEquals(property1.name, "children");
        Assert.assertEquals(property1.defaultValue, "Seq[Children].empty ");
        Assert.assertEquals(property1.baseType, "Seq");
        Assert.assertEquals(property1.containerType, "array");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(property1.isContainer);
    }

    @Test(description = "convert a model with complex map property")
    public void complexMapPropertyTest() {
        final Schema model = new Schema()
                .description("a sample model")
                .addProperties("children", new MapSchema()
                        .additionalProperties(new Schema().$ref("#/definitions/Children")));
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a sample model");
        Assert.assertEquals(cm.vars.size(), 1);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Children")).size(), 1);

        final CodegenProperty property1 = cm.vars.get(0);
        Assert.assertEquals(property1.baseName, "children");
        Assert.assertEquals(property1.complexType, "Children");
        Assert.assertEquals(property1.getter, "getChildren");
        Assert.assertEquals(property1.setter, "setChildren");
        Assert.assertEquals(property1.dataType, "Map[String, Children]");
        Assert.assertEquals(property1.name, "children");
        Assert.assertEquals(property1.defaultValue, "Map[String, Children].empty ");
        Assert.assertEquals(property1.baseType, "Map");
        Assert.assertEquals(property1.containerType, "map");
        Assert.assertFalse(property1.required);
        Assert.assertTrue(property1.isContainer);
        Assert.assertFalse(property1.isNotContainer);
    }

    @Test(description = "convert an array model")
    public void arrayModelTest() {
        final Schema schema = new ArraySchema()
                .items(new Schema().$ref("#/definitions/Children"))
                .description("an array model");
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", schema, Collections.singletonMap("sample", schema));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "an array model");
        Assert.assertEquals(cm.vars.size(), 0);
        Assert.assertEquals(cm.parent, "ListBuffer[Children]");
        Assert.assertEquals(cm.imports.size(), 2);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("ListBuffer", "Children")).size(), 2);
    }

    @Test(description = "convert a map model")
    public void mapModelTest() {
        final Schema model = new Schema()
                .description("a map model")
                .additionalProperties(new Schema().$ref("#/definitions/Children"));
        final DefaultCodegen codegen = new ScalaAkkaClientCodegen();
        final CodegenModel cm = codegen.fromModel("sample", model, Collections.singletonMap("sample", model));

        Assert.assertEquals(cm.name, "sample");
        Assert.assertEquals(cm.classname, "Sample");
        Assert.assertEquals(cm.description, "a map model");
        Assert.assertEquals(cm.vars.size(), 0);
        Assert.assertEquals(cm.parent, "Map[String, Children]");
        Assert.assertEquals(cm.imports.size(), 1);
        Assert.assertEquals(Sets.intersection(cm.imports, Sets.newHashSet("Map", "Children")).size(), 1);
    }

}
