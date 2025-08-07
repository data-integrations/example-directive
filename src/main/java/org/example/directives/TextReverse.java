/*
 *  Copyright © 2017 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

// package org.example.directives;
//
// import io.cdap.cdap.api.annotation.Description;
// import io.cdap.cdap.api.annotation.Name;
// import io.cdap.cdap.api.annotation.Plugin;
// import io.cdap.cdap.api.common.Bytes;
// import io.cdap.wrangler.api.Arguments;
// import io.cdap.wrangler.api.Directive;
// import io.cdap.wrangler.api.ExecutorContext;
// import io.cdap.wrangler.api.Row;
// import io.cdap.wrangler.api.parser.ColumnName;
// import io.cdap.wrangler.api.parser.Expression;
// import io.cdap.wrangler.api.parser.Text;
// import io.cdap.wrangler.api.parser.TokenType;
// import io.cdap.wrangler.api.parser.UsageDefinition;
//
// import java.util.List;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// /**
//  * This class <code>TextReverse</code>implements a <code>Directive</code> interface
//  * for reversing the text specified by the value of the <code>column</code>.
//  */
// @Plugin(type = Directive.TYPE)
// @Name(TextReverse.DIRECTIVE_NAME)
// @Description("Reverses the text represented by the column.")
// public final class TextReverse implements Directive {
//   private static final Logger LOG = LoggerFactory.getLogger(TextReverse.class);
//   public static final String DIRECTIVE_NAME = "copla-bug";
//   // private String column;
//   private String expressionValue;
//   private String textValue1;
//   private String textValue2;
//
//   @Override
//   public UsageDefinition define() {
//     LOG.info("THIS IS A TEST LOG");
//     // Usage : text-reverse :column;
//     UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
//     // builder.define("column", TokenType.COLUMN_NAME);
//     builder.define("condition", TokenType.EXPRESSION);
//     builder.define("some_text", TokenType.TEXT);
//     builder.define("some_text_2", TokenType.TEXT, true);
//     return builder.build();
//   }
//
//   @Override
//   public void initialize(Arguments args) {
//     // column = ((ColumnName) args.value("column")).value();
//     // These lines parse the arguments. The error happens before this code is even
//     // executed on the remote worker because the serialized data is already malformed.
//     expressionValue = ((Expression) args.value("condition")).value();
//     textValue1 = ((Text) args.value("some_text")).value();
//     // Check if the optional argument was provided before trying to access it
//     if (args.contains("some_text_2")) {
//       textValue2 = ((Text) args.value("some_text_2")).value();
//     } else {
//       textValue2 = "DEFAULT"; // Provide a default if it's missing
//     }
//   }
//
//   @Override
//   public List<Row> execute(List<Row> rows, ExecutorContext context) {
//     // for (Row row : rows) {
//     //     //   int idx = row.find(column);
//     //     //   if (idx != -1) {
//     //     //     Object object = row.getValue(idx);
//     //     //     if (object instanceof String) {
//     //     //       String value = (String) object;
//     //     //       String reversed = new StringBuilder(value).reverse().toString();
//     //     //       row.setValue(idx, reversed);
//     //     //     } else if (object instanceof byte[]) {
//     //     //       String value = Bytes.toString((byte[]) object);
//     //     //       String reversed = new StringBuilder(value).reverse().toString();
//     //     //       row.setValue(idx, reversed);
//     //     //     }
//     //     //   }
//     //     // }
//     //     // return rows;
//     // The logic here is trivial; its purpose is just to have a valid directive.
//     LOG.info("Executing directive '{}' on {} rows.", DIRECTIVE_NAME, rows.size());
//     for (Row row : rows) {
//       row.add("repro_output", String.format("Expr: '%s', Text1: '%s', Text2: '%s'",
//           expressionValue, textValue1, textValue2));
//     }
//     return rows;
//   }
//
//   @Override
//   public void destroy() {
//     // no-op
//   }
// }


package org.example.directives;

import io.cdap.cdap.api.annotation.Description;

import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.Expression;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;
import io.cdap.wrangler.expression.EL;
import io.cdap.wrangler.expression.ELException;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin(type = Directive.TYPE)
@Name(TextReverse.NAME)
@Description("Reproduces the framework bug where lineage() is called before initialize().")
public final class TextReverse implements Directive, Lineage {
  private static final Logger LOG = LoggerFactory.getLogger(TextReverse.class);
  public static final String NAME = "copla-bug";
  private String expressionValue;
  private String textValue1;
  private String textValue2; // Added second text value

  // This variable will be null when lineage() is called.
  private EL el;

  @Override
  public UsageDefinition define() {
    // 1. Create a builder variable. Do not chain the calls.
    LOG.debug("Defining usage for directive '{}'.", NAME);
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);

    // 2. Define all arguments to match the customer's UDD.
    builder.define("condition", TokenType.EXPRESSION);
    builder.define("text_arg_1", TokenType.TEXT);
    builder.define("text_arg_2", TokenType.TEXT);

    return builder.build();
  }

  @Override
  public void initialize(Arguments args) {
    LOG.info("Initializing directive '{}'. This message should appear first.", NAME);
    this.expressionValue = ((Expression) args.value("condition")).value();
    this.textValue1 = ((Text) args.value("text_arg_1")).value();
    this.textValue2 = ((Text) args.value("text_arg_2")).value();
    LOG.info("arguments: {}, {}, {}", expressionValue, textValue1, textValue2);
    try {
      this.el = EL.compile(this.expressionValue);
      LOG.info("el :{}", el);
    } catch (ELException e) {
      LOG.error("Failed to compile args:", e);
      // In a real directive, you'd handle this.
    }
  }

  @Override
  public Mutation lineage() {
    LOG.info("Generating lineage for directive '{}'. This message should appear second.", NAME);
    // The framework bug causes this method to run BEFORE initialize().
    // Therefore, 'el' is null, and the next line will throw a NullPointerException.
    try {
      // This line will throw the NullPointerException
      return Mutation.builder()
          .readable("Accessing variables from condition: %s", el.variables())
          .build();
    } catch (NullPointerException e) {
      LOG.error("Caught NullPointerException in lineage() as expected!", e);
      throw e; // Re-throw the exception to trigger the framework failure
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) {
    LOG.info("Executing directive '{}' on {} rows.", NAME, rows.size());
    for (Row row : rows) {
      row.add("repro_output", String.format("Expr: '%s', Text1: '%s', Text2: '%s'",
          expressionValue, textValue1, textValue2));
    }
    return rows;
  }

  @Override
  public void destroy() {
    // no-op
  }
}
