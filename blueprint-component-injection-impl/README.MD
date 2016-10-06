# Quick Overview
This bundle provides the default implementation of the MdcWrapper interface. For further information about the purpose of the API and how to use it, see its [documentation](https://github.com/SourcePond/mdcwrapper-api).

## Installation
The bundle is currently not released and therefore not available from Maven Central.

## Examples
### Get an instance of MdcWrapper

... or with OSGi Blueprint
```
<blueprint xmlns="http://www.osgi.org/xmlns/blueprint/v1.0.0">
	<reference id="wrapper" interface="ch.sourcepond.utils.mdcwrapper.MdcWrapper" />
</blueprint>
```