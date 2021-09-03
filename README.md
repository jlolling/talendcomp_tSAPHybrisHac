# Talend Components tSAPHybrisHac*
Talend Components to read and write data to SAP Hybris E-Commerce Platform via the Hybris Admin Console

Hybris projects needs to implement there own data exchange interfaces.
Out-of-the-box the Hybris Admin Console (HAC) is the possibility to exchange data.

The HAC is a core part of Hybris and always present.
But the HAC only provides a web formular to read/write data.
To automate such actions Klaus Hausschild has created his well known project jhac:
https://github.com/klaushauschild1984/jhac

The Talend components uses this project to provide a convient Talend component to read/write data to Hybris.
At the moment only the read part is implmented: tSAPHybrisHacFlexibleSearch

Flexible Search is the query language of Hybris:
https://help.sap.com/viewer/d0224eca81e249cb821f2cdf45a82ace/6.6.0.0/en-US/8bc399c186691014b8fce25e96614547.html

## Component tSAPHybrisHacFlexibleSearch
This component reads data from Hybris defined by a Flexible Search query (see doc above).

The component connects to the HAC and executes the given query.
The component also translates the names from the Talend schema into the technical field names returned by the query.

![Here an simple example job](https://github.com/jlolling/talendcomp_tSAPHybrisHac/blob/master/doc/tSAPHybrisHacFlexibleSearch_example_job_design.png)
The HAC returns the result fields in case of there is no alias always in lower case with prefix "p_".
If a alias is given it the result field will named axactly like the alias.
The component tests the given Talend columns and maps them to the returned query fields.
If a field cannot be mapped, the component fails with a meaningful error message.

**One important thing: The order of the fields is not important, only the names must match**
This is unlike the database components, where the order and the typ is important and the name does not matter.

## Component tSAPHybrisHacImpexImport
This components use an impex header (must be read before as String (e.g. in a context variable) and reads from in incoming flow from a chosable column the actual impex data csv row and takes all other columns which are set as key columns as correlation keys. These correlation keys are needed later on when we evaluate the response from the import. These correlation keys will provided later to the error messages for in import rows.
The component reads in all incoming rows and at the end the component sends the actual request to the HAC. Therefore it is a good practice to limit the records to about 2000 (depends on the size of one record). 
![Here an simple example job](https://github.com/jlolling/talendcomp_tSAPHybrisHac/blob/master/doc/tSABHybrisHacImpexImport_job_design.png)

There is also a component to execute a SQL query and convert the result convienent into a csv without the need to know the schema.
Please take a look at the project https://github.com/jlolling/talendcomp_tSAPHybrisQueryToImpexCSV
The component tSAPHybrisQueryToImpexCSV can greatly simplify the creation of the impex csv data especcially for a generic approach.

## Component tSAPHybrisHacImpexImportErrors
This component references the component tSAPHybrisHacImpexImport and returns for the last import all errors related to the incoming records.
The component returns also the correlation keys assigned to the error message which could be matched to an incoming record of tSAPHybrisHacImpexImport.
![Here an simple example job](https://github.com/jlolling/talendcomp_tSAPHybrisHac/blob/master/doc/tSAPHybrisHacImpexImportErrors_job_design.png)

## Component tSAPHybrisHacImpexExport
This component use the HAC function to export mass data as csv compressed in a ZIP file.
The component takes a impex header (can also combined in a very special way with a flexible search to select specific records) and provides the result as ZIP file. This zip file contains 2 files: a csv file and a impex file. The csv file contains the data and the impex file contains the used impex header).
![Here an simple example job](https://github.com/jlolling/talendcomp_tSAPHybrisHac/blob/master/doc/tSAPHybrisHacImpexExport_job_design.png)
