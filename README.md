# AI Council 3.81.0

A local absolute-risk neglect check now flags a verdict that treats a relative risk reduction, percentage improvement, or ratio as decisive while never stating the absolute risk difference, baseline rate, or number needed to treat/harm. Naming that gap is not itself an error. The coordinator cannot close solely on relative figures. Version matches Gradle: `3.81.0` / versionCode `249`.

# AI Council 3.80.0

A local motivated-reasoning check now flags a verdict that appears to select, interpret, or weight evidence primarily to protect a preferred conclusion, identity, or prior commitment, rather than updating on the full balance of evidence (including disconfirming data). Naming that gap is not itself an error. The coordinator cannot close solely on a conclusion-preserving reading of the evidence. Version matches Gradle: `3.80.0` / versionCode `248`.

# AI Council 3.79.0

A local collider / Berkson check now flags a verdict that treats an association seen only inside a selected group — admitted, hospitalized, survivors, or respondents — as the association in the full population, without asking whether selection itself created the correlation. Naming that gap is not an error. The coordinator also turns several already-detected hypotheses (small samples, “this time is different”, complexity, Knightian uncertainty, anticipated regret, Simpson, Goodhart, ergodicity, collider) into instructions instead of dropping them. Version matches Gradle: `3.79.0` / versionCode `247`.

# AI Council 3.78.0

A local law-of-small-numbers check now flags a verdict that treats a small sample or handful of observations as highly representative of the underlying population or rate, without naming the sample size, expected variance, confidence interval, or a larger reference class. Naming that gap is not itself an error. The coordinator cannot close a frequency or generalization solely on a small sample. Version matches Gradle: `3.78.0` / versionCode `246`.

# AI Council 3.77.0

A local "this time is different" check now flags a verdict that treats a specific case as immune to the historical base rate or reference class solely because "this time is different", "conditions have changed", or "we are special", without naming the reference class, the base rate, or an explicit mechanism why the difference overrides the outside view. Naming that gap is not itself an error. The coordinator cannot close a numeric forecast or timeline solely on the claim that this case is exceptional. Version matches Gradle: `3.77.0` / versionCode `245`.

# AI Council 3.76.0

A local reference-class forecasting check now flags a verdict that treats a specific forecast or success probability as decisive without naming a reference class of similar past cases, a base rate from that class, or an explicit note that the inside view is being used alone. Naming that gap is not itself an error. The coordinator cannot close a numeric forecast solely on the inside story. Version matches Gradle: `3.76.0` / versionCode `244`.
