package com.aicouncil.app;

import java.util.List;
import java.util.Locale;

/**
 * Extra local deliberation signals (v2.26+): urgency, evidence grade,
 * cost/benefit framing and obligation clashes. No network.
 */
public final class DeliberationSignals {
    private DeliberationSignals() {}

    public static void detectAll(String a, String b, List<String> conflicts) {
        detectAll(null, a, b, conflicts);
    }

    public static void detectAll(String task, String a, String b, List<String> conflicts) {
        detectUrgency(a, b, conflicts);
        detectEvidenceGrade(a, b, conflicts);
        detectCostBenefitFraming(a, b, conflicts);
        detectObligation(a, b, conflicts);
        ReasoningFallacyPolicy.detect(a, b, conflicts);
        DecisionBriefPolicy.detect(a, b, conflicts);
        EvidenceIndependencePolicy.detect(a, b, conflicts);
        SampleEvidencePolicy.detect(a, b, conflicts);
        ExpectedUtilityPolicy.detect(a, b, conflicts);
        CalibrationPolicy.detect(a, b, conflicts);
        PremortemPolicy.detect(a, b, conflicts);
        OutsideViewPolicy.detect(a, b, conflicts);
        SensitivityAnalysisPolicy.detect(a, b, conflicts);
        TailRiskPolicy.detect(a, b, conflicts);
        OpportunityCostPolicy.detect(a, b, conflicts);
        GoodhartPolicy.detect(a, b, conflicts);
        SunkCostPolicy.detect(a, b, conflicts);
        ConfirmationBiasPolicy.detect(a, b, conflicts);
        CausalInferencePolicy.detect(a, b, conflicts);
        ErgodicityPolicy.detect(a, b, conflicts);
        ModelCorrelationPolicy.detect(a, b, conflicts);
        AnchoringPolicy.detect(a, b, conflicts);
        StatusQuoPolicy.detect(a, b, conflicts);
        AvailabilityPolicy.detect(a, b, conflicts);
        RepresentativenessPolicy.detect(a, b, conflicts);
        PlanningFallacyPolicy.detect(a, b, conflicts);
        HindsightBiasPolicy.detect(a, b, conflicts);
        AmbiguityAversionPolicy.detect(a, b, conflicts);
        LossAversionPolicy.detect(a, b, conflicts);
        PrincipalAgentPolicy.detect(a, b, conflicts);
        FalseDilemmaPolicy.detect(a, b, conflicts);
        SurvivorshipBiasPolicy.detect(a, b, conflicts);
        RegressionToMeanPolicy.detect(a, b, conflicts);
        AuthorityBiasPolicy.detect(a, b, conflicts);
        FramingEffectPolicy.detect(a, b, conflicts);
        GamblersFallacyPolicy.detect(a, b, conflicts);
        HaloEffectPolicy.detect(a, b, conflicts);
        ProbeBeforeCommitPolicy.detect(a, b, conflicts);
        PresentBiasPolicy.detect(a, b, conflicts);
        OutcomeBiasPolicy.detect(a, b, conflicts);
        OmissionBiasPolicy.detect(a, b, conflicts);
        ScopeInsensitivityPolicy.detect(a, b, conflicts);
        AffectHeuristicPolicy.detect(a, b, conflicts);
        ConjunctionFallacyPolicy.detect(a, b, conflicts);
        NarrativeFallacyPolicy.detect(a, b, conflicts);
        SecondOrderThinkingPolicy.detect(a, b, conflicts);
        EcologicalFallacyPolicy.detect(a, b, conflicts);
        SelectionBiasPolicy.detect(a, b, conflicts);
        AppealToIgnorancePolicy.detect(a, b, conflicts);
        BaseRateNeglectPolicy.detect(a, b, conflicts);
        SimpsonParadoxPolicy.detect(a, b, conflicts);
        ZeroRiskBiasPolicy.detect(a, b, conflicts);
        ExponentialGrowthBiasPolicy.detect(a, b, conflicts);
        RecencyBiasPolicy.detect(a, b, conflicts);
        SocialProofBiasPolicy.detect(a, b, conflicts);
        FalseConsensusBiasPolicy.detect(a, b, conflicts);
        IllusoryTruthBiasPolicy.detect(a, b, conflicts);
        BeliefBiasPolicy.detect(a, b, conflicts);
        ChestertonFencePolicy.detect(a, b, conflicts);
        OverconfidenceBiasPolicy.detect(a, b, conflicts);
        FundamentalAttributionPolicy.detect(a, b, conflicts);
        SelfServingBiasPolicy.detect(a, b, conflicts);
        CruxPolicy.detect(a, b, conflicts);
        NaiveRealismPolicy.detect(a, b, conflicts);
        OptimismBiasPolicy.detect(a, b, conflicts);
        DunningKrugerPolicy.detect(a, b, conflicts);
        ProjectionBiasPolicy.detect(a, b, conflicts);
        JustWorldBiasPolicy.detect(a, b, conflicts);
        AutomationBiasPolicy.detect(a, b, conflicts);
        EndowmentEffectPolicy.detect(a, b, conflicts);
        MysideBiasPolicy.detect(a, b, conflicts);
        FalsePrecisionPolicy.detect(a, b, conflicts);
        IKEAEffectPolicy.detect(a, b, conflicts);
        FocusingIllusionPolicy.detect(a, b, conflicts);
        PeakEndRulePolicy.detect(a, b, conflicts);
        ActionBiasPolicy.detect(a, b, conflicts);
        NormalcyBiasPolicy.detect(a, b, conflicts);
        BandwagonBiasPolicy.detect(a, b, conflicts);
        IllusionOfControlPolicy.detect(a, b, conflicts);
        ReactiveDevaluationPolicy.detect(a, b, conflicts);
        ReversibilityPolicy.detect(a, b, conflicts);
        StrawManPolicy.detect(a, b, conflicts);
        ZeroSumBiasPolicy.detect(a, b, conflicts);
        UnderdeterminationPolicy.detect(a, b, conflicts);
        MotteAndBaileyPolicy.detect(a, b, conflicts);
        AssumptionLedgerPolicy.detect(a, b, conflicts);
        SemmelweisReflexPolicy.detect(a, b, conflicts);
        ReactancePolicy.detect(a, b, conflicts);
        SlipperySlopePolicy.detect(a, b, conflicts);
        AdHominemPolicy.detect(a, b, conflicts);
        WhataboutismPolicy.detect(a, b, conflicts);
        MovingTheGoalpostsPolicy.detect(a, b, conflicts);
        CircularReasoningPolicy.detect(a, b, conflicts);
        PostHocPolicy.detect(a, b, conflicts);
        ConfusionOfTheInversePolicy.detect(a, b, conflicts);
        MiddleGroundFallacyPolicy.detect(a, b, conflicts);
        AppealToConsequencesPolicy.detect(a, b, conflicts);
        NoTrueScotsmanPolicy.detect(a, b, conflicts);
        HastyGeneralizationPolicy.detect(a, b, conflicts);
        CherryPickingPolicy.detect(a, b, conflicts);
        TexasSharpshooterPolicy.detect(a, b, conflicts);
        AppealToEmotionPolicy.detect(a, b, conflicts);
        NirvanaFallacyPolicy.detect(a, b, conflicts);
        GeneticFallacyPolicy.detect(a, b, conflicts);
        DisconfirmationPolicy.detect(a, b, conflicts);
        BurdenOfProofPolicy.detect(a, b, conflicts);
        SpecialPleadingPolicy.detect(a, b, conflicts);
        FalseEquivalencePolicy.detect(a, b, conflicts);
        PropositionalFallacyPolicy.detect(a, b, conflicts);
        EquivocationPolicy.detect(a, b, conflicts);
        AppealToNaturePolicy.detect(a, b, conflicts);
        AppealToTraditionPolicy.detect(a, b, conflicts);
        AppealToPityPolicy.detect(a, b, conflicts);
        RedHerringPolicy.detect(a, b, conflicts);
        TuQuoquePolicy.detect(a, b, conflicts);
        AppealToNoveltyPolicy.detect(a, b, conflicts);
        SeenAndUnseenPolicy.detect(a, b, conflicts);
        StreetlightPolicy.detect(a, b, conflicts);
        AppealToFearPolicy.detect(a, b, conflicts);
        PersonalIncredulityPolicy.detect(a, b, conflicts);
        CompositionDivisionPolicy.detect(a, b, conflicts);
        WeakAnalogyPolicy.detect(a, b, conflicts);
        AppealToForcePolicy.detect(a, b, conflicts);
        LoadedQuestionPolicy.detect(a, b, conflicts);
        IsOughtPolicy.detect(a, b, conflicts);
        RelativePrivationPolicy.detect(a, b, conflicts);
        FallacyFallacyPolicy.detect(a, b, conflicts);
        GishGallopPolicy.detect(a, b, conflicts);
        IdentifiableVictimPolicy.detect(a, b, conflicts);
        PoisoningTheWellPolicy.detect(a, b, conflicts);
        WoozleEffectPolicy.detect(a, b, conflicts);
        MoralLicensingPolicy.detect(a, b, conflicts);
        DataDredgingPolicy.detect(a, b, conflicts);
        CausalOversimplificationPolicy.detect(a, b, conflicts);
        ImpactBiasPolicy.detect(a, b, conflicts);
        GroupthinkPolicy.detect(a, b, conflicts);
        FrequencyIllusionPolicy.detect(a, b, conflicts);
        IllusionOfExplanatoryDepthPolicy.detect(a, b, conflicts);
        KillCriterionPolicy.detect(a, b, conflicts);
        MissingMechanismPolicy.detect(a, b, conflicts);
        CurseOfKnowledgePolicy.detect(a, b, conflicts);
        PracticalSignificancePolicy.detect(a, b, conflicts);
        AvailabilityCascadePolicy.detect(a, b, conflicts);
        SurrogateEndpointPolicy.detect(a, b, conflicts);
        ClusteringIllusionPolicy.detect(a, b, conflicts);
        NegativityBiasPolicy.detect(a, b, conflicts);
        BeliefPerseverancePolicy.detect(a, b, conflicts);
        ConfoundingPolicy.detect(a, b, conflicts);
        SkinInTheGamePolicy.detect(a, b, conflicts);
        PublicationBiasPolicy.detect(a, b, conflicts);
        IllusoryCorrelationPolicy.detect(a, b, conflicts);
        AttributeSubstitutionPolicy.detect(a, b, conflicts);
        AnswerContractPolicy.detect(task, a, b, conflicts);
        WishfulThinkingPolicy.detect(a, b, conflicts);
        BackfireEffectPolicy.detect(a, b, conflicts);
        BetterThanAveragePolicy.detect(a, b, conflicts);
        HotHandFallacyPolicy.detect(a, b, conflicts);
        McNamaraFallacyPolicy.detect(a, b, conflicts);
        SampleSizeNeglectPolicy.detect(a, b, conflicts);
        ProbabilityNeglectPolicy.detect(a, b, conflicts);
        DenominatorNeglectPolicy.detect(a, b, conflicts);
        MentalAccountingPolicy.detect(a, b, conflicts);
        IllusionOfValidityPolicy.detect(a, b, conflicts);
        DecoyEffectPolicy.detect(a, b, conflicts);
        ValueOfInformationPolicy.detect(a, b, conflicts);
        ExternalValidityPolicy.detect(a, b, conflicts);
        HyperbolicDiscountingPolicy.detect(a, b, conflicts);
        CompromiseEffectPolicy.detect(a, b, conflicts);
        CertaintyEffectPolicy.detect(a, b, conflicts);
        ContrastEffectPolicy.detect(a, b, conflicts);
        DefaultEffectPolicy.detect(a, b, conflicts);
        ChoiceOverloadPolicy.detect(a, b, conflicts);
        DurationNeglectPolicy.detect(a, b, conflicts);
        EscalationOfCommitmentPolicy.detect(a, b, conflicts);
        DisagreementDiagnosisPolicy.detect(task, a, b, conflicts);
        HiddenProfilePolicy.detect(a, b, conflicts);
        UnitBiasPolicy.detect(a, b, conflicts);
        AnecdotalEvidencePolicy.detect(a, b, conflicts);
        KnightianUncertaintyPolicy.detect(a, b, conflicts);
        AnticipatedRegretPolicy.detect(a, b, conflicts);
        MissingCounterfactualPolicy.detect(a, b, conflicts);
        UnintendedConsequencesPolicy.detect(a, b, conflicts);
        CatastrophicRiskPolicy.detect(a, b, conflicts);
        MoralHazardPolicy.detect(a, b, conflicts);
        AdverseSelectionPolicy.detect(a, b, conflicts);
        WinnersCursePolicy.detect(a, b, conflicts);
        InformationCascadePolicy.detect(a, b, conflicts);
        FreeRiderPolicy.detect(a, b, conflicts);
        PathDependencePolicy.detect(a, b, conflicts);
        LucasCritiquePolicy.detect(a, b, conflicts);
        ComplexityBiasPolicy.detect(a, b, conflicts);
        ReferenceClassForecastingPolicy.detect(a, b, conflicts);
        ThisTimeIsDifferentPolicy.detect(a, b, conflicts);
        VerdictClosurePolicy.detect(task, a, b, conflicts);
    }

    /** One expert pushes immediate action; the other wants to wait. */
    static void detectUrgency(String a, String b, List<String> conflicts) {
        int ua = urgencySign(a), ub = urgencySign(b);
        if (ua != 0 && ub != 0 && ua != ub) {
            conflicts.add("\u0441\u0440\u043e\u0447\u043d\u043e\u0441\u0442\u044c: \u0434\u0435\u0439\u0441\u0442\u0432\u043e\u0432\u0430\u0442\u044c \u0441\u0435\u0439\u0447\u0430\u0441 \u043f\u0440\u043e\u0442\u0438\u0432 \u043e\u0442\u043b\u043e\u0436\u0438\u0442\u044c / \u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c");
        }
    }

    private static int urgencySign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean now = lower.contains("\u043d\u0435\u043c\u0435\u0434\u043b\u0435\u043d\u043d\u043e") || lower.contains("\u0441\u0435\u0439\u0447\u0430\u0441") || lower.contains("\u0441\u0440\u043e\u0447\u043d\u043e")
                || lower.contains("\u043a\u0430\u043a \u043c\u043e\u0436\u043d\u043e \u0441\u043a\u043e\u0440\u0435\u0435") || lower.contains("without delay")
                || lower.contains("immediately") || lower.contains("right now") || lower.contains("asap")
                || lower.contains("\u043d\u0435\u0433\u0430\u0439\u043d\u043e") || lower.contains("\u044f\u043a\u043e\u043c\u043e\u0433\u0430 \u0448\u0432\u0438\u0434\u0448\u0435");
        boolean later = lower.contains("\u043f\u043e\u0434\u043e\u0436\u0434\u0430\u0442\u044c") || lower.contains("\u043e\u0442\u043b\u043e\u0436\u0438\u0442\u044c") || lower.contains("\u043d\u0435 \u0441\u0435\u0439\u0447\u0430\u0441")
                || lower.contains("\u043f\u043e\u0437\u0436\u0435") || lower.contains("wait until") || lower.contains("postpone")
                || lower.contains("not yet") || lower.contains("defer") || lower.contains("\u0437\u0430\u0447\u0435\u043a\u0430\u0442\u0438")
                || lower.contains("\u0432\u0456\u0434\u043a\u043b\u0430\u0441\u0442\u0438") || lower.contains("\u043f\u0456\u0437\u043d\u0456\u0448\u0435");
        if (now && later) return 0;
        if (now) return 1;
        if (later) return -1;
        return 0;
    }

    /** Hard evidence (RCT / meta / systematic) vs anecdote / single case. */
    static void detectEvidenceGrade(String a, String b, List<String> conflicts) {
        int ga = evidenceGrade(a), gb = evidenceGrade(b);
        if ((ga >= 2 && gb <= 0) || (gb >= 2 && ga <= 0)) {
            conflicts.add("\u0441\u0438\u043b\u0430 \u0434\u043e\u043a\u0430\u0437\u0430\u0442\u0435\u043b\u044c\u0441\u0442\u0432: \u0441\u0438\u0441\u0442\u0435\u043c\u0430\u0442\u0438\u043a\u0430/RCT \u043f\u0440\u043e\u0442\u0438\u0432 \u0430\u043d\u0435\u043a\u0434\u043e\u0442\u0430 \u0438\u043b\u0438 \u0435\u0434\u0438\u043d\u0438\u0447\u043d\u043e\u0433\u043e \u0441\u043b\u0443\u0447\u0430\u044f");
        }
    }

    private static int evidenceGrade(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        int grade = 0;
        if (lower.contains("meta-analysis") || lower.contains("\u043c\u0435\u0442\u0430-\u0430\u043d\u0430\u043b\u0438\u0437") || lower.contains("\u043c\u0435\u0442\u0430\u0430\u043d\u0430\u043b\u0456\u0437")
                || lower.contains("systematic review") || lower.contains("\u0441\u0438\u0441\u0442\u0435\u043c\u0430\u0442\u0438\u0447\u0435\u0441\u043a") || lower.contains("\u0441\u0438\u0441\u0442\u0435\u043c\u0430\u0442\u0438\u0447\u043d")) {
            grade = 3;
        } else if (lower.contains("randomized") || lower.contains("rct") || lower.contains("\u0440\u0430\u043d\u0434\u043e\u043c\u0438\u0437\u0438\u0440\u043e\u0432\u0430\u043d")
                || lower.contains("\u0440\u0430\u043d\u0434\u043e\u043c\u0456\u0437\u043e\u0432\u0430\u043d")) {
            grade = 2;
        } else if (lower.contains("anecdote") || lower.contains("\u0430\u043d\u0435\u043a\u0434\u043e\u0442") || lower.contains("\u0435\u0434\u0438\u043d\u0438\u0447\u043d\u044b\u0439 \u0441\u043b\u0443\u0447\u0430\u0439")
                || lower.contains("one case") || lower.contains("\u043e\u0434\u0438\u043d \u0441\u043b\u0443\u0447\u0430\u0439") || lower.contains("\u043e\u0434\u0438\u043d \u0432\u0438\u043f\u0430\u0434\u043e\u043a")) {
            grade = 0;
        } else {
            grade = 1;
        }
        return grade;
    }

    /** One frames as cost, the other as benefit / opportunity. */
    static void detectCostBenefitFraming(String a, String b, List<String> conflicts) {
        boolean aCost = costBenefitSign(a) < 0, aBen = costBenefitSign(a) > 0;
        boolean bCost = costBenefitSign(b) < 0, bBen = costBenefitSign(b) > 0;
        if ((aCost && bBen) || (bCost && aBen)) {
            conflicts.add("\u0444\u0440\u0435\u0439\u043c\u0438\u043d\u0433 \u0446\u0435\u043d\u044b/\u0432\u044b\u0433\u043e\u0434\u044b: \u043e\u0434\u0438\u043d \u0430\u043a\u0446\u0435\u043d\u0442 \u043d\u0430 \u0437\u0430\u0442\u0440\u0430\u0442\u0430\u0445, \u0434\u0440\u0443\u0433\u043e\u0439 \u043d\u0430 \u0432\u044b\u0433\u043e\u0434\u0430\u0445");
        }
    }

    private static int costBenefitSign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean cost = lower.contains("\u0437\u0430\u0442\u0440\u0430\u0442") || lower.contains("cost") || lower.contains("\u0446\u0435\u043d\u0430")
                || lower.contains("\u0434\u043e\u0440\u043e\u0433\u043e") || lower.contains("expensive") || lower.contains("\u0432\u0438\u0442\u0440\u0430\u0442");
        boolean benefit = lower.contains("\u0432\u044b\u0433\u043e\u0434") || lower.contains("benefit") || lower.contains("\u043f\u0440\u0438\u0431\u044b\u043b")
                || lower.contains("\u0434\u0451\u0448\u0435\u0432\u043e") || lower.contains("cheap") || lower.contains("\u0435\u043a\u043e\u043d\u043e\u043c\u0456\u044f");
        if (cost && benefit) return 0;
        if (cost) return -1;
        if (benefit) return 1;
        return 0;
    }

    /** Obligation / must vs optional / can. */
    static void detectObligation(String a, String b, List<String> conflicts) {
        int oa = obligationSign(a), ob = obligationSign(b);
        if (oa != 0 && ob != 0 && oa != ob) {
            conflicts.add("\u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u043e\u0441\u0442\u044c: \u00ab\u043d\u0430\u0434\u043e/\u0434\u043e\u043b\u0436\u0435\u043d\u00bb \u043f\u0440\u043e\u0442\u0438\u0432 \u00ab\u043c\u043e\u0436\u043d\u043e/\u043e\u043f\u0446\u0438\u043e\u043d\u0430\u043b\u044c\u043d\u043e\u00bb");
        }
    }

    private static int obligationSign(String text) {
        String lower = text == null ? "" : text.toLowerCase(Locale.ROOT);
        boolean must = lower.contains("\u0434\u043e\u043b\u0436\u0435\u043d") || lower.contains("\u043d\u0430\u0434\u043e") || lower.contains("\u043e\u0431\u044f\u0437\u0430\u043d")
                || lower.contains("must") || lower.contains("have to") || lower.contains("required")
                || lower.contains("\u043f\u043e\u0432\u0438\u043d\u0435\u043d") || lower.contains("\u0442\u0440\u0435\u0431\u0430");
        boolean optional = lower.contains("\u043c\u043e\u0436\u043d\u043e") || lower.contains("\u043e\u043f\u0446\u0438\u043e\u043d\u0430\u043b\u044c\u043d\u043e") || lower.contains("\u043d\u0435 \u043e\u0431\u044f\u0437\u0430\u0442\u0435\u043b\u044c\u043d\u043e")
                || lower.contains("optional") || lower.contains("can") || lower.contains("\u043c\u043e\u0436\u043d\u0430")
                || lower.contains("\u043d\u0435 \u043e\u0431\u043e\u0432\u044f\u0437\u043a\u043e\u0432\u043e");
        if (must && optional) return 0;
        if (must) return 1;
        if (optional) return -1;
        return 0;
    }
}
