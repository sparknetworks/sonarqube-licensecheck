package at.porscheinformatik.sonarqube.licensecheck.interfaces;

import at.porscheinformatik.sonarqube.licensecheck.model.Dependency;

import java.io.File;
import java.util.List;

/***
 * Interface defining a scanner that retrieves Dependency information from an external model
 */
public interface Scanner
{
    /***
     * Returns a list of dependencies as found in the module provided
     * @param moduleDir path to the module to scan
     * @return list of found dependencies or empty list
     */
    List<Dependency> scan(File moduleDir);
}
