# AI Council 3.94.0

The coordinator map is restored: on-device cues again tell the synthesizer which open gap blocks a label, instead of a blank placeholder. A new local check flags a verdict that closes a choice without saying which mistake is more expensive — a false start or a missed chance. The decision frame now lists that asymmetry as a closable gap. A «стоит ли …?» prompt still becomes action versus status quo, but the request to close the choice is no longer swallowed into the action. Naming the gap is not itself an error. Fluency and rhyme still cannot close a choice by themselves. Version matches Gradle: `3.94.0` / versionCode `262`.

# AI Council 3.93.0

A local rhyme-as-reason / processing-fluency check now flags a verdict that treats a fluent, easy-to-process, catchy, or rhyming claim as more true or decisive solely because of its form or fluency, without independent evidence or mechanism. Naming that gap is not itself an error. The coordinator cannot close solely on processing fluency or rhyme. Version matches Gradle: `3.93.0` / versionCode `261`.

# AI Council 3.92.0

Decision frame gains two more closable gaps for decision tasks: opportunity cost (what is given up / next-best alternative) and pre-mortem / kill criterion (failure mode + stop condition). Binary prompts of the form «стоит ли X?» / «should we X?» now extract the action and status quo as the two live options. The on-device check surfaces the stronger frame without a network call. A verdict that still closes while the top gap is open remains only a hypothesis. Naming the gap is not itself an error. Version matches Gradle: `3.92.0` / versionCode `260`.

# AI Council 3.91.0

A local decision frame is now computed from the question itself before any model speaks. It names the asked question, extracts explicit alternatives and magnitudes, and lists the missing ingredients of a closable choice: options (including leaving things as they are), scale, time horizon, who bears the loss, base rate, and a success metric. Experts receive that frame, and the on-device check shows it without a network call. A verdict that still closes while the top gap is open is only a hypothesis. Naming the gap is not itself an error. Key-assumption and threshold-effect signals now also reach the coordinator, which previously ignored them. Version matches Gradle: `3.91.0` / versionCode `259`.
