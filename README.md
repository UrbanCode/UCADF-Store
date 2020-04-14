# UCADF Store
The UrbanCode Application Deployment Framework (UCADF) Store is a directory structure and a set of utilities that facilitates management of the UrbanCode Deploy instances and the UCADF packages required for those instances.

Note: All of the instructions and examples are based on using the Git bash shell on a Windows machine. Most of the commands/utilities/examples have not been tested in a Windows (DOS) shell.

### Related Projects
The [UCADF-Core-Library](https://github.com/UrbanCode/UCADF-Core-Library), [UCADF-Core-Plugin](https://github.com/UrbanCode/UCADF-Core-Plugin) projects, and the [ABC-UCADF](Development/ABC-UCADF) README provide related information.

## Preparing to Use the UCADF Store
Open a Bash shell and run commands similar to the following:
```
# Set the UCADF_STORE environment variable, example:
export UCADF_STORE=/c/Dev/git/UCADF-Store

# Clone the UCADF Store by using a command similar to the following to clone the Git project.
git clone [UCADF Store Git project] "$UCADF_STORE"
```

## Prerequisites for Downloading from Maven
Some of the UCADF Store management tasks require pulling from Maven repositories. To do these tasks Maven must be installed and the Maven settings files configured appropriately to access the repositories needed for download.

# Directory Structure
The UCADF-Store has the following directory structure.

## /Instances Directory
This is the directory where the UrbanCode instances are defined. It contains one directory per instance with that directory containing the instance-specific property files. Here's the an example instance with example configuration files.
```
/Instances
  /example
    /instance.properties
    /instance.secure.properties
    /ABC-UCADF-Package.secure.properties
```

***instance.properties***
```
# The UCD intance URL.
ucdUrl=https://myucdinstance.org

# The type of the instance: Development, Test, UAT, Production.
ucAdfEnvType=Development
```

***instance.secure.properties***
```
# The instance admin token.
ucdAuthToken=MyAuthToken

# The UCD instance email user ID and password.
ucdEmailUserId=MyEmailUserId
ucdEmailUserPw=MyEmailUserPw
```

***ABC-UCADF-Package.secure.properties***
```
# Static properties.
ucAdfOrchestrationUserId=ABCOrchestration
ucAdfOrchestrationUserPw=MyPassword

# This property is automatically added to this file when the UCADF is pushed and the UCADF orchestration user and auth token are created.
ucAdfOrchestrationToken=MyToken
```

## /Management Directory
This directory contains UCADF management utilities.
```
/Management
  (Management utility files.)
```

## /Packages Directory
This directory where any required UCADF packages are downloaded and extracted. The general structure of a UCADF package directory is:
```
/Packages
  /[PackageName]-Package
    /[PackageVersion]
      /Actions
          (Package-specific action files.)
      /Applications
        /[ExportedUrbanCodeApplicationName]
          (Package-specific Application export files.)
      /Libraries
        /[Version]
          (Package-specific library version files.)
      /NotificationTemplates
        (Package-specific notification templates files.)
      /Test
        (Package-specific test files.)
```

### Downloading a UCADF Package from Maven
Use a command similar to the following to download a UCADF package from Maven and unzip it.
```
mvn clean -f "$UCADF_STORE/Management/DownloadPackage-pom.xml" -DpackageGroupId=org.urbancode.ucadf.core -DpackageArtifactId=UCADF-Core-Package -DpackageVersion=1.0.0
```

## /Plugins Directory
This is the directory where any required UrbanCode automation and source configuration plugin version files are located. There should be one file per plugin version.
```
/Plugins
  /UCADF-Core-v[version].zip
  (Any other plugin files needed to support the UCADFs.)
```

### Downloading a UCADF Plugin from Maven
A command similar to the following can be used to download a UCADF plugin from Maven.
```
mvn clean -f "$UCADF_STORE/Management/DownloadPlugin-pom.xml" -DpluginGroupId=org.urbancode.ucadf.core -DpluginArtifactId=UCADF-Core-Plugin -DpluginVersion=1.0.0
```

### Acquiring UrbanCode Deploy Plugins Not Published to Maven
UrbanCode Deploy proprietary plugins must be acquired and downloaded to this directory.

# Running ucadfclient Commands
The ucadfclient is the command line utility that runs action files. Here's how to set up to use the command:
```
# Set PATH to include the UCADF-Core-Package library directory.
export PATH=$PATH:$UCADF_STORE/Packages/UCADF-Core-Package/1.0.0/Library

# Make sure the ucadfclient command works.
ucadfclient
ERROR: Filename is required. Use the -f option.
```

***Adding Additional UCADF Packages to the ucadfclient Class Path***

If all of the actions to be executed are in the ***UCADF-Core-Library*** then no additional class path is needed. But if actions are needed from additional UCADF libraries then the ***UCADF_PACKAGES_CLASSPATH*** environment variable must be set so that the ***ucadfclient*** knows where to find the libraries.
```
export UCADF_PACKAGES_CLASSPATH="c/DEV/git/UCADF-Store/Development/ABC-UCADF/target/ABC-UCADF-1.0.0-SNAPSHOT.jar"
```
Note: When an UrbanCode process is running a ***UCADF-Core-Plugin*** step, the plugin manages the classpath automatically as it downloads the specified UCADF package versions.

# UrbanCode Deploy Instance Management
The UCADF actions can be used to automate management of UrbanCode instances.

These examples are using two instances that would be in the /Instances directory:
```
/Instances
  /ucadfdev - An instance where UCADF functionality is developed.
  /ucadftest - An instance to which UCADF functionality is deployed and tested.
```

## Setting up a New UrbanCode Instance
When a new UrbanCode instance is installed there can be many configuration tasks that need to be done. These can vary greatly between organizations but here are some starter actions for consideration.

***System Configuration***

(TBD) Mail, etc.

***Authentication/Authorization Realms***

(TBD) LDAP, etc.

# Managing UCADF Core in an Instance
THe UCADF core provides the functionality needed to use UCADF actions. There
* Generic agents.
* Security actions.
* The UCADF-Core-Plugin.
* The UCADF-Core-Library

## Generic Agents for an UrbanCode Instance
The UCADF uses a term called ***Generic Agents*** to refer to one or more (a pool) of agents that will be used to run steps that aren't required to be run on a target agent for the the purpose of deploying to that target agent.

These example UCADF action files rely on at least one generic agent being available with a prefix of ***generic:*** that identified it as a generic agent.

The generic agents pool is then added to the resource tree as ***/GenericAgents*** where it can be used by UrbanCode processes as needed.

## Generating UCADF Package Versions
It's important to keep in mind that the ***UCADF-Core-Plugin*** will only download versions of packages that it has not previously downloaded. During a development/test cycle there may be a need for changes to the UCADF package files that are pushed to the instance. To accomodate this the push action files can auto-generate a package version at push time.

## Pushing the UCADF Core to an Instance
The UCADF Core is pushed to an instance by running an actions file using a command similar to the following:
```
# To use a specified version number.
ucadfclient -f $UCADF_STORE/Management/Actions/UcAdfCorePush.yml -DucAdfInstance=ucadftest -DpackageVersion=1.0.0 -DpackageDir=$UCADF_STORE/Packages/UCADF-Core-Package/1.0.0 -DcorePluginVersion=1.0.0

# To auto-generate a version number.
ucadfclient -f $UCADF_STORE/Management/Actions/UcAdfCorePush.yml -DucAdfInstance=ucadftest -DpackageDir=$UCADF_STORE/Packages/UCADF-Core-Package/1.0.0 -DcorePluginVersion=1.0.0
```
This actions file:
- Runs the security actions file.
- Installs the ***UCADF-Core-Plugin*** using the version specified in instance.properties.
- Creates a ***UC Support*** team and add administrators to the ***Public*** team.
- Creates a ***GenericAgent*** agent tag and adds it to the generic agents with the ***generic:*** prefix.
- Adds generic agents to the ***Public*** team.
- Creates a ***GenericAgentsPool*** agent pool, adds the generic agents to it, and adds the pool to the ***Public*** team.
- Adds all agent relays to the ***Public*** team for visibility.
- Runs the ***UcAdfCorePushArtifacts.yml*** actions file to push the package artifacts to the instance as a component version.

***To Subsequently Push Just the Package Artifacts***

After the first full push has been done then to push the package artifacts without running all of the other actions a command similar to the following can be used:
```
# To use a specified version number.
ucadfclient -f $UCADF_STORE/Management/Actions/UcAdfCorePushArtifacts.yml -DucAdfInstance=ucadftest -DpackageVersion=1.0.0 -DpackageDir=$UCADF_STORE/Packages/UCADF-Core-Package/1.0.0

# To auto-generate a version number.
ucadfclient -f $UCADF_STORE/Management/Actions/UcAdfCorePushArtifacts.yml -DucAdfInstance=ucadftest -DpackageDir=$UCADF_STORE/Packages/UCADF-Core-Package/1.0.0
```
This actions file:
- Creates a UCADF-Core-Package component.
- If not ***packageVersion*** property specified then generates a new package version.
- Creates a UCADF-Core-Package component version.
- Uploads the files from the ***packageDir***.

## Deleting the UCADF Core from an Instance
The UCADF Core actions can be (mostly) reversed from the changes done by the UCADF Core push. The security actions will not be reversed unless extra actions are added to capture them before the push.
```
ucadfclient -f $UCADF_STORE/Management/Actions/UcAdfCoreDelete.yml -DucAdfInstance=ucadftest
```

# The UCADF Lifecycle
Now that the UCADF Core is in place it can be used to run plugin steps that use the core or to develop and implement UCADFs that have a broader scope of managing a given platform type. 

***This is all explained in the example [ABC-UCADF](./Development/ABC-UCADF).***

# About Encryption Keys
UCADFs files pulle from an instance may contain encrypted values that are using a keystore that is not on the UrbanCode instance to which the UCADF is to be deployed. At this time there is no API that provides a way to install encryption keys. Thus, the UrbanCode administrator must add the keys to the keystore on the target UrbanCode instance and restart the UrbanCode service to pick up the changes.

# About Notification Templates
UCADFs may contain notification templates but at this time there is no API that provides a way to install notification templates. An UrbanCode administrator must install the template files on the UrbanCode instance and restart the UrbanCode service to pick up the changes.

# License
This project is licensed under the MIT License - see the [LICENSE](../LICENSE) file for details
 
