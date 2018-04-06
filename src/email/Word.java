package email;

import java.math.BigDecimal;

public class Word {
	private BigDecimal spamProbability = new BigDecimal(0); // probability that the word belongs to the spam class.
	private BigDecimal hamProbability = new BigDecimal(0);  // probability that the word belongs to the ham (non-spam) class.
	private String word = "";                                  // the string representation of the word.
	
	public Word(String word){
		this.word = word;
	}
	
	public String getWord() {
		return word;
	}

	public BigDecimal getSpamProbability() {
		return spamProbability;
	}

	public void setSpamProbability(BigDecimal spamProbability) {
		this.spamProbability = spamProbability;
	}

	public BigDecimal getHamProbability() {
		return hamProbability;
	}

	public void setHamProbability(BigDecimal hamProbability) {
		this.hamProbability = hamProbability;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((word == null) ? 0 : word.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Word other = (Word) obj;
		if (word == null) {
			if (other.word != null)
				return false;
		} else if (!word.equals(other.word))
			return false;
		return true;
	}
}
