package org.tud.minitimetable;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.tud.minitimetable.model.IhtcModel;
import org.tud.minitimetable.util.FileWriter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

public class Prototyp2 {

	private static class MZSolution {
		public final long timestamp;
		public final Object result;

		public MZSolution(long timestamp, Object result) {
			this.timestamp = timestamp;
			this.result = Objects.requireNonNull(result);
		}

	}

	private static class MZOutputParser implements Closeable {
		private InputStream input;

		public MZOutputParser(InputStream mzOutput) {
			this.input = Objects.requireNonNull(mzOutput);
		}

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}
	}

	private static final Path miniZincLocation = Path.of("..", "..", "..", "MiniZinc", "minizinc.exe");
	private static final Path resourceFolder = Path.of("./", "resources");

	private static Path changeFileExtension(Path filePath, String newExtension) {
		String fileName = filePath.getFileName().toString();
		int extensionDelimiter = fileName.lastIndexOf('.');

		if (extensionDelimiter > 0) {
			int newExtensionStart = newExtension.startsWith(".") ? 1 : 0;
			String newfileName = fileName.substring(0, extensionDelimiter + 1)
					+ newExtension.substring(newExtensionStart);
			return filePath.resolveSibling(newfileName);
		}

		return filePath;
	}

	private static Path getUserDirectory() {
		String currentDir = System.getProperty("user.dir");
		return Path.of(currentDir);
	}

	private static Path getResourceDirectory() {
		return getUserDirectory().resolve(resourceFolder).normalize();
	}

	private static Path getMiniZinc() {
		return getUserDirectory().resolve(miniZincLocation).normalize();
	}

	private static Path getValidator() {
		return getResourceDirectory().resolve("IHTP_Validator_win.exe");
	}

	private static void transformInput() {
		Path modelFolder = getResourceDirectory().resolve("ihtc");
		Path outputFolder = getResourceDirectory().resolve("out");

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Path constraintModel = getResourceDirectory().resolve("IHTC_model.mzn");

		String modelName = "i01";
		Path modelFile = getResourceDirectory().resolve("ihtc").resolve(modelName + ".json");
		IhtcModel model = null;// loadInputModel(modelFile);

		var miniZincDataFile = changeFileExtension(modelFile, ".dzn");
		try (var buffer = Files.newBufferedWriter(miniZincDataFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING); var writer = new FileWriter(buffer)) {
			// model.toMiniZinc(writer);
		}

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(getUserDirectory().resolve("mini").toFile());
		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectInput(Redirect.INHERIT);
//		processBuilder.redirectOutput(Redirect.INHERIT);

//		processBuilder.inheritIO();
//		processBuilder.redirectOutput(getUserDirectory().resolve("mini").resolve("miniout.dzn").toFile());
//		processBuilder.redirectOutput().
		processBuilder.command().add("\"" + getMiniZinc().toAbsolutePath().toString() + "\"");
//		System.out.println(getMiniZinc().toAbsolutePath().toString());
//		processBuilder.command().add("\"" + constraintModel.toString() + "\"");

//		Path sM = Path.of("F:").resolve("Test").resolve("IHTC_model.mzn");
//		Path sD = Path.of("F:").resolve("Test").resolve("i01.dzn");
//
//		processBuilder.command().add("--param-file F:/Test/model.mpc");

//		System.out.println("--model \"" + ".\\" + sM + "\"");
		processBuilder.command().add("\"" + constraintModel.toString() + "\"");
		processBuilder.command().add("\"" + miniZincDataFile.toString() + "\"");
//		processBuilder.command().add("--help");
		processBuilder.command().add("--json-stream");

//		var processCmd = "\"" + getMiniZinc().toAbsolutePath().toString() + "\"";
//		processBuilder.command(processCmd, getResourceDirectory().resolve("model.mpc").toString());

		CompletableFuture<Process> isDone = null;
		try {
			var miniZincProcess = processBuilder.start();
			isDone = miniZincProcess.onExit();

			var outStream = miniZincProcess.getInputStream();
			System.out.println("Found  stream: " + outStream);
			final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

			String lastLine = outReader.readLine();
			while (lastLine != null) {
				// json output

				var json = (JSONObject) JSON.parse(lastLine);
				if ("solution".equals(json.getString("type"))) {
					var output = ((JSONObject) json.get("output")).getString("default");
					var solution = new MZSolution(System.currentTimeMillis(), output);
					System.out.println(solution);
				}

				lastLine = outReader.readLine();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isDone != null) {
			long maxTime = 5000;
			long sleepTime = 250;
			while (!isDone.isDone() && maxTime > 0) {
				try {
					maxTime -= sleepTime;
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (!isDone.isDone()) {
				try {
					System.out.println("Process did not terminate: " + isDone.get().pid());
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.err.println("Error?");
		}

	}

}
