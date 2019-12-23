# ABC-UCADF
This is an example UrbanCode Application Deployment Framework (UCADF) that elaborates on some self-service concepts and shows how a platform-specific UCADF can be developed to manage application setup and deployment.

### Related Projects
The [UCADF-Core-Library](https://github.com/UrbanCode/UCADF-Core-Library), [UCADF-Core-Plugin](https://github.com/UrbanCode/UCADF-Core-Plugin) projects, and the [UCADF-Store](../README.md) README provide related information.

## The UCADF Naming Convention
In order to automate the management of UCADFs and allow multiple UCADFs to exist in a given instance, there is a naming standard used to identify the entities that are part of the UCADF. In this case the prefix ***ABC*** is used on the names of the common entities used to manage the UCADF.

# The ABC-UCADF Package
This example goes through the processes required to produce and consume a UCADF package that contains the set of functionality needed to facilitate a specific pattern of application management ande deployment in an UrbanCode Deploy instance.

## The ABC-UCADF Package Actions
The ***[ABC-UCADF Package Actions](./UCADF-Package/Actions)*** provide practical usage examples of the UCADF actions. Those should be referenced as going through the following documentation.

## The Security Model
The security model is a layered self-service model with the following roles.

| Roles | Description |
|:----- |:----------- |
| UrbanCode administrator | Manage the UrbanCode infrastructure, the UCADFs, and defines the members of ABC Administrator role. Only UranCode administrators change processes. |
| ABC Administrator | A consumer of the UCADF that is responsible for the overall management. If the ABC-UCADF represents a given platform type then this would could be members of the platform team that manages that platform. |
| ABC Suite Administrator | Has the ability to manage suite-level entities that represent a subset of the platform ***infrastructure***. Suites identify the infrastructure targets to which applications would be able to deploy. |
| ABC Application Manager | Has the ability to manage entities that represent the application team's ***applications*** that can potentially be deployed to targets defined in the suite infrastructure. Applications can be created independent of the infrastructure then added into a given location in the suite infrastructure by the suite team at a later date. |
| ABC Application Deployer-Test | Has the ability to deploy applications to Test environments. |
| ABC Application Deployer-Production | Has the ability to deploy applications to Production environments. |
| ABC Application Builder | Has the ability to create application team-owned component versions. This is associated with an internal ***ABC AppTeam \[ApplicationTeamName\] Builder*** account and has an associated token to be used by CI/CD pipelines. |
| Public Team | Has the ability to view common entities that aren't suite team or application team specific. |

## The "ABC Management" Application
The ABC Management application is not a real application but rather a way to provide self-service management processes to the suite and application teams. In order to provide this self-service functionality the ABC Management Application has these environments that are created/deleted automatically by the UCADF processes.

| Environment Name | Description |
|:-------------- |:----- |
| Management | The environment on which to run processes not related to an existing suite or application team. |
| SuiteTeam \[SuiteTeamName\] | The environment on which to run suite team-specific processes to add/remove resources for their suite. |
| AppTeam \[ApplicationTeamName\] | The environment on which to run application team-specific processes to add/remove applications. |

### The "ABC-UCDAssembly" Application
Some of the functionality of the ABC UCADF makes it difficult to use the UrbanCode Application Template functionality. To overcome this the ABC UCADF has the ***ABC-UCDAssembly*** application. This is used as the basis for creating and managing the applicaiton processes in application team applications created by the ABC UCADF. When changes are made to the assembly they can be replicated across the other applications based on that assembly by running the ABC Management application ***UCADF-Resync All Application Processes from Application Assembly*** process.

### The "ABC Management" Component
This component has processes that perform the management-type processing for the UCADF. This component uses the ***ABC-Template*** component template in order to have properties from that template at run time.

### The "ABC-Template" Component Template
This component template is the used by all of the components in the ABC UCADF. This is where many of the UCADF's properties are located so that they can be available to any of the child component processes. Any processes non-management processes needed across all of the application components are also in this template.

### The Component Processes
The ABC UCADF has most of its functionality externalized to action files that are then run by the component process using the ***UCADF-Core-Plugin*** to run the action files. Here's an example of the YAML provided in one of the plugin steps. The YAML from the action files could just have well been placed directly in the step YAML instead of a file.
```
propertyFiles:
  - fileName: "${u:actionPackages/ABC-UCADF-Package/actionsDirectoryName}/AbcUcAdfActionProperties.yml"

actions:
  - action: UcAdfRunActionsFromFile
    propertyValues:
      ucdUserId: ""
      ucdUserPw: "${p:ucAdfAdminToken}"
      appTeamBaseName: "${p:abcTeamName}"
    fileName: "AbcCreateAppTeam.yml"
```

***Using Elevated Access in Processes***

The example above uses a property called ***ucAdfAdminToken*** that comes from the a cmoponent template property. This method allows the step to have elevated access to update the UrbanCode instance that the user doesn't have in the context of running a process.

### The ABC UCADF Actions
The UCADF-Package/Actions directory contains the actions files run by the processes.

The UCADF-Package/Library directory (at runtime) will contain a the ABC-UCADF jar file that has the ***AbcValidateDeploy*** action. This example action is a stub that provides an example of how the UCADF Core can be extended by providing custom actions.

## The Resource Tree
``` 
/ABC
  /Instances
    /Production
      /[ProviderName]
        /[SuiteName]
          /[InstanceName]
            /[ApplicationName]
              /[ApplicationComponentName]
    /Test
      ...
  /Management
    /Lists
      /Instances
        /Production
          /[ProviderName]
            /[SuiteName]
              /[InstanceName]
```

## The Shadow Resource Tree
The ABC UCADF uses a ***/Management/Lists*** shadow resource tree to allow the suite team to manage credential/connection type properties about a target. This allows the application team to still have the ability to manage properties in their application resoruce tree without being able to override the instance-specific properties.

## The Deployment Process
The ABC UCADF deployment process is simulating an over-the-wire deployment where the processing runs on a generic agent rather than an agent on a target application server. The application deployment process is doing a two-phase deployment where it first pushes out the application to a target then activates it as a separate step.

- Deploy the components tagged as order 1 components.
- Deploy the components tagged as order 2 components.
- (Not in the example but there could be a task here to approve activation after checkout is complete.)
- Activate the components tagged as order 1 components.
- Activate the components tagged as order 2 components.

### The "Run Provider Action" Process
All of the steps above use this process.

- Downloads the component version artifacts.
- Replaces tokens in the artifacts which allows properties to be injected at run time.
- Uses a generic process to inject the shadow resource tree properties into a component process properties that can then be used for the deployment.
- Creates a UCProcess.props file that has any additional properties.
- If the particular action requires getting cached artifacts then it downloads them from the cache component version. (More about cache actions in the section below.)
- Runs the ***provider*** action. In this example the provider is a Docker image that has the functionality to do the over-the-wire deployment to the target application instance. This allows the Docker image to be maintained by the target platform team outside of UrbanCode and means that layer can be updated as needed without any changes being required within UrbanCode.
- If the particular action requires storing cached artifacts then create a cache component version with those artifacts.

## About The UCADF Cache Actions
This deployment example runs multiple steps that could potentially be executed on different generic agents in a generic agent pool. The deploy step generates temporary artifacts that are needed by the activation step. The UCADF caching actions provide a way to handle these temporary artifacts.

- The ***UcAdfPutCacheComponentVersion*** action creates a ***\[ComponentName\]-Cache*** component version, uploads the artifacts to the version, and sets version properties to indicate which application process is using the artifacts.
- The ***UcAdfGetCacheComponentVersion*** action gets the ***\[ComponentName\]-Cache*** component version associated with the application process and downloads the temporary artifacts.
- When processing is complete the component version is deleted. IMPORTANT: The cache component version should not be used directly by an UrbanCode process or it cannot be deleted.

# Developing the UCADF
The UCADF is developed using an UrbanCode development instance.

| Development Phase | Description |
|:-------- |:------ |
| Design/Prototyping | Use the UrbanCode Deploy UI to create the required process and experiment with the design that will be needed to make the functionality achieve its goal. This typically means manually creating one or more components, applications, environment, resources, teams, roles, etc. |
| Set Up Security | Use the UrbanCode Deploy UI to create/configure the roles, types, and permissions required for the UCADF. |
| Develop Action Files<br>(or plugin text) | When the design is understood then the UCADF action files may be developed outside of the instance using a YAML editor to create the files. It's also possible to just put the action YAML directly in the ***UCADF-Core-Plugin*** step text rather than externalizing to action files. |
| Develop Custom Actions | If custom actions are needed then they will need to be developed as Groovy (compiled) or Java code. |
| Test the Actions | Most UCADF actions files can be tested by running them from the command line using the ucadfclient command and/or the UCADF-Core-Plugin steps within UrbanCode. |
| Integrate the Processes and Actions | Change the UrbanCode processes as needed to run the desired UCADF actions. Run each manually to validate the integration. This may require building/deploying the UCADF package many times during testing. |
| Create Process Tests | Create UCADF action files that automate running end-to-end testing of the UCADF processes. |
| Pull the UCADF Information from the Developement Instance | Pull the UCADF's security actions from the instance to a security actions file and pull the UCADF's Management and Assembly applications from the instance to application export files. |
| Build the UCADF Package | Build the UCADF package zip file from the contents of the package directory. |

## Setting the Working UCADF Package Directory
The examples in this guide use a variable called ***ABC_PACKAGEDIR*** that needs to be set according to what work is being done.
```
# To push from a development directory use this.
# For development, hote that any files required for the /Library directory will need to be copied there.
export ABC_PACKAGEDIR=$UCADF_STORE/Development/ABC-UCADF/UCADF-Package

# To push from a downloaded package directory use this.
export ABC_PACKAGEDIR=$UCADF_STORE/Packages/ABC-UCADF/1.0.0/UCADF-Package
```

## Pulling the UCADF Information from an Instance
The UCADF processes and security are pulled from an instance and saved to files that can be used for later pushes by running a command similar to the following:
```
ucadfclient -f $ABC_PACKAGEDIR/Actions/AbcUcAdfPull.yml -DucAdfInstance=ucadftest -DucAdfPackageDir=$ABC_PACKAGEDIR
```
This actions file:
- Creates a security actions file. The name matching patterns specified in the pull actions include the ***Administrator*** and ***Public Viewer*** roles and permissions.
- Exports the ***ABC Management*** and ***ABC-UCDAssembly*** applications, creates a supplemental ****.actions.json*** for each of the them, and writes the required encryption key names to the ***application.properties*** file.

## Building the UCADF Package
The Maven build requires the following properties to be defined.

| Property | Description |
|:-------------- |:--------- |
| MVN_REPO_ID | The Maven repository ID. |
| MVN_REPO_NAME | The Maven repository name. |
| MVN_REPO_URL | The Maven repository URL. |

Use the following commands to run the build and deploy the jar to Maven:<br>
```
export MVN_REPO_ID=MyRepoId
export MVN_REPO_NAME=MyRepoName
export MVN_REPO_URL=MyRepoURL

# Define the version number. If building with Jenkins the last digit could be the Jenkins ${BUILD_NUMBER}.
export ABC_UCADF_VERSION=1.0.0

# Set the version number in the pom.xml file.
mvn versions:set -DnewVersion="$ABC_UCADF_VERSION"

# Build and package. There's no need to deploy because this is not a shared library.
mvn -U package
```

## Publishing the UCADF Package
If the package needs to be published to Maven then after the build completes use the following commands to build the ABC-UCADF-Package zip file and deploy it to Maven:<br>
```
# Set the version number in the package-pom.xml file.
mvn -f package-pom.xml versions:set -DnewVersion="$ABC_UCADF_VERSION"

# Build and deploy the ABC-UCADF-Package.zip file.
mvn -f package-pom.xml package deploy:deploy-file -Dfile="target/ABC-UCADF-Package-$ABC_UCADF_VERSION.zip" -DgeneratePom=false -DgroupId=org.urbancode.ucadf.abc -DartifactId=ABC-UCADF-Package -Dversion="$ABC_UCADF_VERSION" -DrepositoryId="$MVN_REPO_ID" -Durl="$MVN_REPO_URL"
```

# Managing the ABC UCADF Across Instances
Once the ABC UCADF package has been created then it can be deployed across multiple instances.

## Downloading the ABC UCADF Package from Maven
Use a command similar to the following to download a UCADF package from Maven and unzip it.
```
mvn clean -f "$UCADF_STORE/Management/DownloadPackage-pom.xml" -DUCADF_STORE="$UCADF_STORE" -DpackageGroupId=org.urbancode.ucadf.abc -DpackageArtifactId=ABC-UCADF-Package -DpackageVersion=1.0.0
```

## Pushing the ABC UCADF to an Instance
The UCADF is pushed to the instance by running an actions file using a command similar to the following.
``` 
ucadfclient -f $ABC_PACKAGEDIR/Actions/AbcUcAdfPush.yml -DucAdfInstance=ucadftest -DpackageDir=$ABC_PACKAGEDIR -DcorePackageVersion=1.0.0
```
This actions file:
- Runs the security actions file.
- Creates an internal orchestration user account.
- Creates a token for the internal orchestration user account and stores the token in the *.secure.properties file. Changes their preferences to use one team by default.
- Creates teams and adds administration members.
- Creates version and snapshot statuses.
- Imports the ABC Management and ABC-UCD Assembly applications and runs the supplemental application actions.
- 
***To Subsequently Push Just the Package Artifacts***

After the first full push has been done then to push the package artifacts without running all of the other actions a command similar to the following can be used:
```
# To use a specified version number.
ucadfclient -f $ABC_PACKAGEDIR/Actions/UcAdfCorePushArtifacts.yml -DucAdfInstance=ucadftest -DpackageVersion=1.0.0 -DpackageDir=$ABC_PACKAGEDIR

# To auto-generate a version number.
ucadfclient -f $ABC_PACKAGEDIR/Actions/UcAdfCorePushArtifacts.yml -DucAdfInstance=ucadftest -DpackageDir=$ABC_PACKAGEDIR
```
This actions file:
- Creates a UCADF-Core-Package component.
- If not ***packageVersion*** property specified then generates a new package version.
- Creates a UCADF-Core-Package component version.
- Uploads the files from the ***packageDir***.

## Running the ABC UCADF Process Tests on a Test Instance
The ***[ABC UCADF Process Tests](./UCADF-Package-Test/Test/Actions/ProcessTests.yml)*** go through various scenarios by running UrbanCode Deploy processes in the instance.
```
ucadfclient -f $ABC_PACKAGEDIR/Test/Actions/ProcessTests.yml -DucAdfInstance=ucadftest -DucAdfPackageDir=$ABC_PACKAGEDIR
```
If a process fails then you must go into the UrbanCode UI and look at the process failure to determine why it failed. The problem can typically be solved as follows:

- If the problem is something within an UrbanCode process itself then it must be fixed through the UrbanCode UI.
- If the problem is with an externalized actions file then you must fix the actions file and push the actions package to the instance.
- Depending on the process logic, you may be able to run the process again from the UI to validate the fixes before running the process test actions again.
- Note: If you make changes to processes or security in the test instance then you'll need to pull them from the test instance and push them back to the development instance to keep the development instance in sync.

## Final ABC UCADF Validation
- Run AbcUcAdfDelete.yml to remove everything about the UCADF from the test instance.
- Run AbcUcAdfPush.yml to push the UCADF to the test instance.
- Run ProcessTests.yml to validate all of the end-to-end processing.

## Deleting the ABC UCADF from an Instance
The UCADF actions can be (mostly) reversed from the changes done by the push. The security actions will not be reversed unless extra actions are added to capture them before the push.
```
ucadfclient -f $ABC_PACKAGEDIR/Actions/AbcUcAdfDelete.yml -DucAdfInstance=ucadftest -DpackageDir=$ABC_PACKAGEDIR
```
