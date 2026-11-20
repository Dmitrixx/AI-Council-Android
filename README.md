# AI Council 3.93.0

A local rhyme-as-reason / processing-fluency check now flags a verdict that treats a fluent, easy-to-process, catchy, or rhyming claim as more true or decisive solely because of its form or fluency, without independent evidence or mechanism. Naming that gap is not itself an error. The coordinator cannot close solely on processing fluency or rhyme. Version matches Gradle: `3.93.0` / versionCode `261`.

# AI Council 3.92.0

Decision frame gains two more closable gaps for decision tasks: opportunity cost (what is given up / next-best alternative) and pre-mortem / kill criterion (failure mode + stop condition). Binary prompts of the form «стоит ли X?» / «should we X?» now extract the action and status quo as the two live options. The on-device check surfaces the stronger frame without a network call. A verdict that still closes while the top gap is open remains only a hypothesis. Naming the gap is not itself an error. Version matches Gradle: `3.92.0` / versionCode `260`.

# AI Council 3.91.0

A local decision frame is now computed from the question itself before any model speaks. It names the asked question, extracts explicit alternatives and magnitudes, and lists the missing ingredients of a closable choice: options (including leaving things as they are), scale, time horizon, who bears the loss, base rate, and a success metric. Experts receive that frame, and the on-device check shows it without a network call. A verdict that still closes while the top gap is open is only a hypothesis. Naming the gap is not itself an error. Key-assumption and threshold-effect signals now also reach the coordinator, which previously ignored them. Version matches Gradle: `3.91.0` / versionCode `259`.
