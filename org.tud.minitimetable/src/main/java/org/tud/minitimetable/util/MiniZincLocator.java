package org.tud.minitimetable.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MiniZincLocator {

	private Collection<Path> _lookup = new ArrayList<>();
	private boolean _stopAfterFirst = true;

	public void setFindAllLocations(boolean value) {
		_stopAfterFirst = !value;
	}

	public void addFolder(Path folder) {
		_lookup.add(folder.normalize());
	}

	public Collection<Path> searchMiniZinc() {
		Collection<Path> results = new ArrayList<>();

		if (!(_stopAfterFirst && !results.isEmpty())) {
			searchLookups(results);
		}
		if (!(_stopAfterFirst && !results.isEmpty())) {
			searchRegistry(results);
		}
		if (!(_stopAfterFirst && !results.isEmpty())) {
			searchWorkingDirectory(results);
		}

		return results;
	}

	private void searchWorkingDirectory(Collection<Path> results) {
		var userDir = System.getProperty("user.dir");
		var workingDirectory = Path.of(userDir);
		var collection = Collections.singleton(workingDirectory);
		searchCollection(collection, results);
	}

	private void searchLookups(Collection<Path> results) {
		searchCollection(_lookup, results);
	}

	private void searchCollection(Collection<Path> folders, Collection<Path> results) {
		for (Path path : folders) {
			if (isValidMiniZincLocation(path)) {
				results.add(path);
			} else {
				try {
					Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							if (isValidMiniZincLocation(file)) {
								results.add(file);
								return FileVisitResult.TERMINATE;
							}
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private boolean isValidMiniZincLocation(Path path) {
		var name = path.getFileName().toString();
		var validName = "minizinc.exe".equals(name);
		return Files.isRegularFile(path) && validName;
	}

	private void searchRegistry(Collection<Path> results) {
		// TODO Auto-generated method stub

	}

}