package eu.scasefp7.eclipse.servicecomposition.operation.ipr;

import java.util.ArrayList;
import java.util.List;

public class ReputationSchema {
	//reputation of the service overall and per country
	private float overallReputationScore ;

	private List<PerCountryReputation> perCountryReputation = new ArrayList<PerCountryReputation>();

	/**
	 * @return the overallReputationScore
	 */
	public float getOverallReputationScore() {
		return overallReputationScore;
	}

	/**
	 * @param overallReputationScore
	 *            the overallReputationScore to set
	 */
	public void setOverallReputationScore(float overallReputationScore) {
		this.overallReputationScore = overallReputationScore;
	}

	/**
	 * @return the perCountryReputation
	 */
	public List<PerCountryReputation> getPerCountryReputation() {
		return perCountryReputation;
	}

	/**
	 * @param perCountryReputation
	 *            the perCountryReputation to set
	 */
	public void setPerCountryReputation(
			List<PerCountryReputation> perCountryReputation) {
		this.perCountryReputation = perCountryReputation;
	}

	public ReputationSchema clone() {
		ReputationSchema rep = new ReputationSchema();
		rep.setOverallReputationScore(this.getOverallReputationScore());
		for (int i = 0; i < this.getPerCountryReputation().size(); i++) {
			rep.getPerCountryReputation().add(
					this.getPerCountryReputation().get(i).clone());

		}
		return rep;
	}

}
