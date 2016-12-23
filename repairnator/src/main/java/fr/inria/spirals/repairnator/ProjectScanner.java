package fr.inria.spirals.repairnator;

import com.sun.tools.internal.xjc.Language;
import fr.inria.spirals.jtravis.entities.Build;
import fr.inria.spirals.jtravis.entities.BuildStatus;
import fr.inria.spirals.jtravis.entities.Repository;
import fr.inria.spirals.jtravis.helpers.RepositoryHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class aims to provide utility methods to scan the projects and get failing builds
 *
 * @author Simon Urli
 */
public class ProjectScanner {
    /**
     * Utility method to read the file and return the list of slug name
     * @param path A path to a file formatted to contain a slug name of project per line (ex: INRIA/spoon)
     * @return the list of slug name as strings
     * @throws IOException
     */
    private static List<String> readSlugProjectFromFilepath(String path) throws IOException {
        File file = new File(path);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> projectsSlug = new ArrayList<String>();
        while (reader.ready()) {
            projectsSlug.add(reader.readLine().trim());
        }
        return projectsSlug;
    }

    /**
     * Take a filepath as input containing a list of projects to scan. Check last build of each project. And finally returns the list of failing builds.
     *
     * @param path A path to a file formatted to contain a slug name of project per line (ex: INRIA/spoon)
     * @return a list of failing builds
     * @throws IOException
     */
    public static List<Build> getListOfFailingBuildFromProjects(String path) throws IOException {
        List<String> slugs = readSlugProjectFromFilepath(path);
        List<Build> result = new ArrayList<Build>();

        for (String slug : slugs) {
            Launcher.LOGGER.debug("Get repo "+slug);
            Repository repo = RepositoryHelper.getRepositoryFromSlug(slug);
            if (repo != null) {
                Build lastBuild = repo.getLastBuild();
                if (lastBuild != null) {
                    Launcher.LOGGER.debug("Examinate repo "+slug+" - build "+lastBuild.getId());
                    if (lastBuild.getConfig().getLanguage().equals("java")) {
                        Launcher.LOGGER.debug("Accept repo "+slug+" - build "+lastBuild.getId()+" - Status : "+lastBuild.getBuildStatus().name());
                        if (lastBuild.getBuildStatus() == BuildStatus.FAILED || lastBuild.getBuildStatus() == BuildStatus.ERRORED) {
                            result.add(lastBuild);
                        }
                    } else {
                        Launcher.LOGGER.warn("Examine repo "+slug+" Careful the following build "+lastBuild.getId()+" is not in java but language: "+lastBuild.getConfig().getLanguage());
                    }

                } else {
                    Launcher.LOGGER.info("It seems that the repo "+slug+" does not have any Travis build.");
                }

            } else {
                Launcher.LOGGER.warn("Can't examine repo : "+slug);
            }
        }

        return result;
    }
}
