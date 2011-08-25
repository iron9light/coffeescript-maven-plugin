Coffeescript Maven Plugin

Configuration options shown are default values and can be ignored for normal use.

    <build>
      <plugins>
        <plugin>
          <groupId>iron9light</groupId>
          <artifactId>coffeescript-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
          <configuration>
            <srcDir>${basedir}/src/main/webapp</srcDir>
            <outputDir>${basedir}/src/main/webapp</outputDir>
            <bare>false</bare>
            <modifiedOnly>false</modifiedOnly>
          </configuration>
          <executions>
            <execution>
              <id>coffeescript</id>
              <goals>
                <goal>compile</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
