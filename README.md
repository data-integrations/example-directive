# Introduction

CDAP provides extensive support for user defined directives (UDDs) as a way to specify custom processing for DataPrep in CDAP. CDAP UDDs can currently be implemented in Java.

CDAP provides the most extensive support for Java functions. Java functions are also more efficient because they are implemented in the same language as CDAP and DataPrep and, also because additional interfaces and integrations with other CDAP subsystems are supported.

User Defined Directives, also known as UDDs, allow you to create custom functions to transform records within CDAP DataPrep a.k.a Wrangler. CDAP comes with a comprehensive library of functions. There are however some omissions, and some specific cases for which UDDs are the solution.

UDDs, similar to User-defined Functions (UDFs) have a long history of usefulness in SQL-derived languages and other data processing and query systems.  While the framework can be rich in their expressiveness, there is just no way they can anticipate all the things a developer wants to do.  Thus, the custom UDF has become commonplace in our data manipulation toolbox. In order to support customization or extension, CDAP now has the ability to build your own functions for manipulating data through UDDs.

Developing CDAP DataPrep UDDs is by no means any rocket science, and is an effective way of solving problems that could either be downright impossible, or does not meet your requirements or very akward to solve. 

## Developing UDD

There is one simple interface for developing your customized directive. The simple interface `co.cask.wrangler.api.Directive` can be used for developing user defined directive.

### Simple API

Building a UDD with the simpler UDD API involves nothing more than writing a class with three functions (evaluate) and few annotations. Here is an example:

```
@Plugin(type = UDD.Type)
@Name(SimpleUDD.NAME)
@Categories(categories = { "example", "simple" })
@Description("My first simple user defined directive")
public SimpleUDD implements Directive {
  public static final String NAME = "my-simple-udd";
  
  public UsageDefinition define() {
    ...
  }
  
  public void initialize(Arguments args) throws DirectiveParseException {
    ...
  }
  
  public List<Row> execute(List<Row> rows, RecipeContext context) throws RecipeException, ErrorRowException {
    ...
  }
}
```

### Testing a simple UDD

Because the UDD is a simple three functions class, you can test it with regular testing tools, like JUnit.

```
public class SimpleUDDTest {

  @Test
  public void testSimpleUDD() throws Exception {
    TestRecipe recipe = new TestRecipe();
    recipe("parse-as-csv :body ',';");
    recipe("drop :body;");
    recipe("rename :body_1 :simpledata;");
    recipe("!my-simple-udd ...");
    
    TestRows rows = new TestRows();
    rows.add(new Row("body", "root,joltie,mars avenue"));
    RecipePipeline pipeline = TestingRig.pipeline(RowHash.class, recipe);
    List<Row> actual = pipeline.execute(rows.toList());
  }
}
```

### Building a UDD Plugin

There is nothing much to be done here - this example repository includes a maven POM file that is pre-configured for building the directive JAR. All that a developer does is build the project using the following command. 

```
  mvn clean package
```

This would generate two files

  * Artifact - `my-simple-udd-1.0-SNAPSHOT.jar`
  * Artifact Configuration `my-simple-udd-1.0-SNAPSHOT.json`
  
### Deploying Plugin

There are multiple ways in which the custom directive can be deployed to CDAP. The two popular ways are through using CDAP CLI (command line interface) and CDAP UI.

#### CDAP CLI

In order to deploy the directive through CLI, start the CDAP CLI and use the `load artifact` command to load the plugin artifact into CDAP. 

```
$ $CDAP_HOME/bin/cdap cli
cdap > load artifact my-simple-udd-1.0-SNAPSHOT.jar config-file my-simple-udd-1.0-SNAPSHOT.json
```

#### CDAP UI
![alt text](https://github.com/hydrator/example-directive/blob/develop/docs/directive-plugin.gif "Logo Title Text 1")

## Example

We will now walk through the creation of a user defined directive(udd) called `text-reverse` that takes one argument: Column Name -- it is the name of the column in a `Row` that needs to be reversed. The resulting row will have the Column Name specified in the input have reversed string of characters.

```
 !text-reverse :address
 !text-reverse :id
```

Here is the implementation of the above UDD. 

```
@Plugin(type = UDD.Type)
@Name(TextReverse.NAME)
@Categories(categories = {"text-manipulation"})
@Description("Reverses the column value")
public final class TextReverse implements UDD {
  public static final String NAME = "text-reverse";
  private String column;
  
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    return builder.build();
  }
  
  public void initialize(Arguments args) throws DirectiveParseException {
    this.column = ((ColumnName) args.value("column").value();
  }
  
  public List<Row> execute(List<Row> rows, RecipeContext context) throws RecipeException, ErrorRowException {
    for(Row row : rows) {
      int idx = row.find(column);
      if (idx != -1) {
        Object object = row.getValue(idx);
        if (object instanceof String) {
          String value = (String) object;
          row.setValue(idx, new StringBuffer(value).reverse().toString());
        }
      }
    }
    return rows;
  }
}
```

### Code Walk Through

#### Annontations

Following annotations are required for the plugin. If any of these are missing, the plugin or the directive will not be loaded. 

* `@Plugin` defines the type of plugin it is. For all UDDs it's set to `UDD.Type`.
* `@Name` defines the name of the plugin and as well as the directive name. 
* `@Description` provides a short description for the plugin and as well as for the directive. 

#### Call Pattern

The call pattern of UDD is the following :

* **DEFINE** : During configure time either in the CDAP Pipeline Transform or Data Prep Service, the `define()` method is invoked only once to retrieve the information of the usage. The usage defines the specification of the arguments that this directive is going to accept. In our example of `text-reverse`, the directive accepts only one argument and that is of type `TokenType.COLUMN_NAME`.
* **INITIALIZE** : During the initialization just before pumping in `Row`s through the directive, the `initialize()` method is invoked. This method is passed the arguments that are parsed by the system. It also provides the apportunity for the UDD writer to validate and throw exception if the value is not as expected.
* **EXECUTE** : Once the pipeline has been setup, the `Row` is passed into the `execute()` method to transform. 

### Testing

Here is the JUnit class that couldn't be any simpler. 

```
  @Test
  public void testBasicReverse() throws Exception {
    TestRecipe recipe = new TestRecipe();
    recipe.add("parse-as-csv :body ',';");
    recipe.add("set-headers :a,:b,:c;");
    recipe.add("text-reverse :b");

    TestRows rows = new TestRows();
    rows.add(new Row("body", "root,joltie,mars avenue"));
    rows.add(new Row("body", "joltie,root,venus blvd"));

    RecipePipeline pipeline = TestingRig.pipeline(TextReverse.class, recipe);
    List<Row> actual = pipeline.execute(rows.toList());

    Assert.assertEquals(2, actual.size());
    Assert.assertEquals("eitloj", actual.get(0).getValue("b"));
    Assert.assertEquals("toor", actual.get(1).getValue("b"));
  }
```

## That's it! Happy UDD'ing.

 
  


