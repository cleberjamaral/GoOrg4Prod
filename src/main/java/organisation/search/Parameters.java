package organisation.search;

public class Parameters {

	// max throughput allowed, in case of excess goal/role must be broken 
	private static double maxThroughput = 8;
	// max workload allowed, in case of excess goal/role must be broken 
	private static double maxWorkload = 8;

	// Minimal penalty for creating a new state
	private static int minimalPenalty = 1;
	// Cost penalty used to infer bad decisions on search
	private static int defaultPenalty = 10;
	// Cost penalty used to infer VERY bad decisions on search
	private static int extraPenalty = defaultPenalty * 2;

	public static double getMaxDataAmount() {
		return maxThroughput;
	}

	public static void setMaxDataAmount(double maxThroughput) {
		Parameters.maxThroughput = maxThroughput;
	}

	public static double getMaxWorkload() {
		return maxWorkload;
	}

	public static void setMaxWorkload(double maxWorkload) {
		Parameters.maxWorkload = maxWorkload;
	}

	public static int getMinimalPenalty() {
		return minimalPenalty;
	}

	public static int getDefaultPenalty() {
		return defaultPenalty;
	}

	public static void setDefaultPenalty(int defaultPenalty) {
		Parameters.defaultPenalty = defaultPenalty;
	}

	public static int getExtraPenalty() {
		return extraPenalty;
	}

}
