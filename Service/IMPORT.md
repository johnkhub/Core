Import
======

Data format
-----------

* Data can be split over as many files as you like as long as the format is correct
* The first column in the file must be the asset type
* The ordering of other column does not matter
 
Process
-------
 
* For each file, the import system will make one pass through the system for each asset type
* Extra passes over and above these are made for extra data such as external identifiers
* An exception file, containing the rows that failed to import, is generated for each pass

> This approach is of course much slower than a single pass, but it makes the implementation so much easier as
> this makes it easier to ensure that say Envelopes, are imported before asset types that are children of the Envelope
>It also makes it easier to use the same DTOs within the service and the Importers 

* Importers **shall** use the REST endpoints implemented by the service to implement imports.  This ensures that:
    * Business rules are applied consitently
    * Audit and other logging is consistent
    * Is arguably less word to implement
    * Arguably test coverage is better by exercising the same code paths 
    
 Configuration
 -------------
ADD CONFIG DOCUMENTATION HERE 
 
 
  
