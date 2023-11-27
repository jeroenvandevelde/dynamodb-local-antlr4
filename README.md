# Reproducible test of the antlr issue with dynamodblocal

When you run the test in com.example.dynamdblocalantlr4.DynamoAntlrTest.test, you will see that it fails with a Exception java.lang.UnsupportedOperationException: java.io.InvalidClassException: org.antlr.v4.runtime.atn.ATN; Could not deserialize ATN with version 3 (expected 4).

When you document the hibernate dependency in the pom.xml, marked with '<!-- START: Document this to let the test succeed,'.
The test will succeed as there is no mismatching antlr version anymore.
