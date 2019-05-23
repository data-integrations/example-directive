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

package org.example.directives;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * This class <code>TextReverse</code>implements a <code>Directive</code> interface
 * for reversing the text specified by the value of the <code>column</code>.
 */
@Plugin(type = Directive.TYPE)
@Name(TextLength.DIRECTIVE_NAME)
@Description("Computes tthe length of the string.")
public final class TextLength implements Directive {
  public static final String DIRECTIVE_NAME = "text-length";
  private String column;

  @Override
  public UsageDefinition define() {
    // Usage : text-reverse :column;
    UsageDefinition.Builder builder = UsageDefinition.builder(DIRECTIVE_NAME);
    builder.define("column", TokenType.COLUMN_NAME);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) {
    column = ((ColumnName) args.value("column")).value();
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) {
    for (Row row : rows) {
      int idx = row.find(column);
      if (idx != -1) {
        Object object = row.getValue(idx);
        if (object instanceof String) {
          String value = (String) object;
          row.setValue(idx, value.length());
        } else if (object instanceof byte[]) {
          row.setValue(idx, ((byte[]) object).length);
        }
      }
    }
    return rows;
  }

  @Override
  public void destroy() {
    // no-op
  }
}
