/**
 * Copyright © 2010-2020 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsonschema2pojo.rules;

import java.io.PrintWriter;
import java.io.FileWriter;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.jsonschema2pojo.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.codemodel.JDocCommentable;
import com.sun.codemodel.JFieldVar;

/**
 * Applies the "required" schema rule.
 *
 * @see <a
 *      href="http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7">http://tools.ietf.org/html/draft-zyp-json-schema-03#section-5.7</a>
 */
public class RequiredRule implements Rule<JDocCommentable, JDocCommentable> {

    private final RuleFactory ruleFactory;

    private static int[] counters = new int[5];

    protected RequiredRule(RuleFactory ruleFactory) {
        this.ruleFactory = ruleFactory;
    }

    /**
     * Applies this schema rule to take the required code generation steps.
     * <p>
     * The required rule simply adds a note to the JavaDoc comment to mark a
     * property as required.
     *
     * @param nodeName
     *            the name of the schema node for which this "required" rule has
     *            been added
     * @param node
     *            the "required" node, having a value <code>true</code> or
     *            <code>false</code>
     * @param parent
     *            the parent node
     * @param generatableType
     *            the class or method which may be marked as "required"
     * @return the JavaDoc comment attached to the generatableType, which
     *         <em>may</em> have an added not to mark this construct as
     *         required.
     */
    @Override
    public JDocCommentable apply(String nodeName, JsonNode node, JsonNode parent, JDocCommentable generatableType, Schema schema) {

        if (node.asBoolean()) {
          counters[0]++; // If1-true
            generatableType.javadoc().append("\n(Required)");

            if (ruleFactory.getGenerationConfig().isIncludeJsr303Annotations()
                    && generatableType instanceof JFieldVar) {
              counters[1]++; // If2-true
                ((JFieldVar) generatableType).annotate(NotNull.class);
            }

            if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()
                    && generatableType instanceof JFieldVar) {
              counters[2]++; // If3-true
                ((JFieldVar) generatableType).annotate(Nonnull.class);
            }
        } else {
          counters[3]++; // If1-else
            if (ruleFactory.getGenerationConfig().isIncludeJsr305Annotations()
                    && generatableType instanceof JFieldVar) {
              counters[4]++; // If4-true
                ((JFieldVar) generatableType).annotate(Nullable.class);
            }
        }

        writeCoverage();

        return generatableType;
    }

    private void writeCoverage() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("ad-hoc.log"));
            writer.printf("If1-true: %d\n", counters[0]);
            writer.printf("If2-true: %d\n", counters[1]);
            writer.printf("If3-true: %d\n", counters[2]);
            writer.printf("If1-else: %d\n", counters[3]);
            writer.printf("If4-true: %d\n", counters[4]);
            writer.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}
