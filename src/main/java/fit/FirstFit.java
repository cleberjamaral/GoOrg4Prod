package fit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author cleber
 *
 */
public class FirstFit extends Fit {

	@Override
	public Map<Requirement,Resource> fitRequirements(String orgName, Set<Requirement> requirements, Set<Resource> resources) {
		Map<Requirement,Resource> matches = new HashMap<>();
		Set<Resource> availableResources = new HashSet<>(resources);
		requirements.forEach(req -> {
			Resource toRemove = null;
			Iterator<Resource> it = availableResources.iterator();
			while (it.hasNext()) {
				Resource res = it.next();
				List<String> rr = new ArrayList<>(req.getFeatures());
				List<String> rs = new ArrayList<>(res.getFeatures());
				if (rs.containsAll(rr)) {
					matches.put(req, res);
					toRemove = res;
					break;
				}
			}
			if (toRemove != null)
				availableResources.remove(toRemove);
		});
		return matches;
	}

}
