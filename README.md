# Introduction

CDAP provides extensive support for user defined directives (UDDs) as a way to specify custom processing for dataprep. CDAP UDDs can currently be implemented in Java.

The most extensive support is provided for Java functions. Java functions are also more efficient because they are implemented in the same language as CDAP and DataPrep and because additional interfaces and integrations with other CDAP subsystems are supported.

User Defined Directives, also known as UDD, allow you to create custom functions to transform records within CDAP DataPrep or a.k.a Wrangler. CDAP comes with a comprehensive library of functions. There are however some omissions, and some specific cases for which UDDs are the solution.

UDDs, similar to User-defined Functions (UDFs) have a long history of usefulness in SQL-derived languages and other data processing and query systems.  While the framework can be rich in their expressiveness, there's just no way they can anticipate all the things a developer wants to do.  Thus, the custom UDF has become commonplace in our data manipulation toolbox. In order to support customization or extension, CDAP now has the ability to build your own functions for manipulating data through UDDs.

Developing CDAP DataPrep UDDs by no means rocket science, and is an effective way of solving problems that could either be downright impossible, or does not meet your requirements or very akward to solve. 

## Developing UDD

There is one simple interface for developing your customized directive. The simple interface `co.cask.wrangler.api.Directive` can be used for developing user defined directive.

### Simple API

Building a UDD with the simpler UDD API involves nothing more than writing a class with three function (evaluate) and few annotations. Here is an example:

```
@Plugin(type = UDD.Type)
@Name(SimpleUDD.NAME)
@Description("My first simple user defined directive")
public SimpleUDD implements Directive {
  public static final String NAME = "my-simple-udd";
  
  public UsageDefinition define() {
    ...
  }
  
  public void initialize(Arguments args) {
    ...
  }
  
  public List<Row> execute(List<Row> rows, RecipeContext context) {
    ...
  }
}
```

### Testing a simple UDD

Because the UDD is simple three functions class, you can test it with regular testing tools, like JUnit.

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

There is nothing much to be done here, this example repository includes a maven POM file that is pre-configured for building the directive JAR. All that a developer does it build the project using the following command. 

```
  mvn clean package
```

This would generate two files

  * Artifact - `my-simple-udd-1.0-SNAPSHOT.jar`
  * Artifact Configuration `my-simple-udd-1.0-SNAPSHOT.json`
  
### Deploying Plugin

#### CDAP UI
Inline-style: 
![alt text](https://github.com/hydrator/example-directive/blob/develop/docs/directive-plugin.gif "Logo Title Text 1")

#### CDAP CLI

In order to deploy the directive through CLI. Start the CDAP CLI and use the `load artifact` command to load the plugin artifact into CDAP. 

```
$ $CDAP_HOME/bin/cdap cli
cdap > load artifact my-simple-udd-1.0-SNAPSHOT.jar config-file my-simple-udd-1.0-SNAPSHOT.json
```
  
  


