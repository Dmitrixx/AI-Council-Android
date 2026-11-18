# AI Council 3.74.0

A local outcome-bias check now flags a verdict that treats the quality of a past decision as determined solely by its outcome (success or failure) without evaluating the decision process, the information available at the time, or base rates of success for similar choices. Naming that gap is not itself an error. The coordinator cannot close solely because it worked or failed. Version matches Gradle: `3.74.0` / versionCode `242`.

# AI Council 3.73.0

A local survivorship-bias check now flags a verdict that treats visible successes, "those who made it", or winner stories as decisive without accounting for the invisible failures, dropout rate, or base rate of non-successes in the same class. Naming that gap is not itself an error. The coordinator cannot close solely on the visible successes. Version matches Gradle: `3.73.0` / versionCode `241`.

# AI Council 3.72.0

The council now checks whether a closing label actually closed the verdict. `ДЕЛАТЬ` or `НЕ ДЕЛАТЬ` without an observation that would flip the conclusion, and without saying whether the step is reversible, is marked unfinished. A forecast stated as one percent, with no range and no reference class, is marked unfinished too. Naming the gap silences the check. The coordinator is told this before it picks a label, and the finished report carries a local note when the label is still hollow. Version matches Gradle: `3.72.0` / versionCode `240`.
