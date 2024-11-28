package org.openrewrite.apache.httpclient5;

import org.openrewrite.*;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.java.tree.TypeUtils;

public class UsernamePasswordCredentials extends Recipe {
    private static final String FQN = "org.apache.http.auth.UsernamePasswordCredentials";
    private static final String METHOD_PATTERN = FQN + " <constructor>(String, String)";

    @Override
    public @NlsRewrite.DisplayName String getDisplayName() {
        return "Migrate UsernamePasswordCredentials to httpclient5";
    }

    @Override
    public @NlsRewrite.Description String getDescription() {
        return "Migrate UsernamePasswordCredentials to httpclient5.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(
          new UsesMethod<>(METHOD_PATTERN),
          new JavaIsoVisitor<ExecutionContext>() {
              @Override
              public J.NewClass visitNewClass(J.NewClass newClass, ExecutionContext ctx) {
                  newClass = super.visitNewClass(newClass, ctx);
                  if (TypeUtils.isOfType(newClass.getType(), JavaType.buildType(FQN))) {
                      newClass = JavaTemplate.builder(newClass.getArguments().get(1).printTrimmed() + ".toCharArray()")
                        .imports("java.util.Arrays")
                        .build()
                        .apply(getCursor(), newClass.getArguments().get(1).getCoordinates().replace());
                  }
                  return newClass;
              }
          });
    }
}
