import os
import glob
import anthropic

def gather_source_code():
    code_content = ""
    for root, _, files in os.walk("src"):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                with open(file_path, "r", encoding="utf-8") as f:
                    code_content += f"\n--- {file_path} ---\n"
                    code_content += f.read()
    return code_content

def call_claude_validator(source_code):
    api_key = os.getenv("ANTHROPIC_API_KEY")
    if not api_key:
        print("ERROR: ANTHROPIC_API_KEY environment variable not set.")
        return None

    client = anthropic.Anthropic(api_key=api_key)

    prompt = f"""
    You are an independent governance and review layer (Claude Validation Agent).
    Your role is:
    - architecture reviewer
    - QA governance reviewer
    - enterprise readiness validator
    - coverage auditor

    Review the following Java Selenium BDD framework for an Applitools-like Visual Validation Engine.
    
    You must validate:
    - Missing workflows, edge cases, negative scenarios
    - Security gaps
    - Performance and reliability concerns
    - False positive risks and baseline corruption risks
    - Weak self-healing logic

    Provide your response in 3 sections:
    1. Validation Report
    2. Missing Component List
    3. Actionable Improvement Recommendations

    Source Code:
    {source_code}
    """

    print("Sending source code to Claude API for validation...")
    try:
        response = client.messages.create(
            model="claude-haiku-4-5-20251001",
            max_tokens=4000,
            temperature=0,
            system="You are a strict Enterprise Java Architect and QA Governance Reviewer.",
            messages=[
                {"role": "user", "content": prompt}
            ]
        )
        return response.content[0].text
    except Exception as e:
        print(f"Failed to call Claude: {e}")
        return None

if __name__ == "__main__":
    code = gather_source_code()
    feedback = call_claude_validator(code)
    
    if feedback:
        with open("claude_feedback.md", "w", encoding="utf-8") as f:
            f.write(feedback)
        print("Claude validation complete! Feedback saved to claude_feedback.md")
