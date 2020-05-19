package fit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FirstFit extends Fit {

	@Override
	public double fitRequirements(String orgName, Set<Requirement> requirements, Set<Resource> resources) {
		Map<Requirement,Resource> match = new HashMap<>();
		Set<Resource> availableResources = new HashSet<>(resources);
		requirements.forEach(req -> {
			Resource toRemove = null;
			Iterator<Resource> it = availableResources.iterator();
			while (it.hasNext()) {
				Resource res = it.next();
				List<String> rr = new ArrayList<>(req.getFeatures());
				List<String> rs = new ArrayList<>(res.getFeatures());
				if (rs.containsAll(rr)) {
					match.put(req, res);
					toRemove = res;
					break;
				}
			}
			if (toRemove != null)
				availableResources.remove(toRemove);
		});
		
		return (double) match.keySet().size() / (double) requirements.size();
	}

}
