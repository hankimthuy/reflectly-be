# Branch Protection Setup

Apply these settings on GitHub for both `reflectly-be` and `reflectly-fe` repositories.

## Recommended rules for `main`

1. **Require a pull request before merging**
   - Require approvals: 1 (optional for solo maintainer)
   - Require review from Code Owners: off

2. **Require status checks to pass**
   - Backend: `build` (from `ci.yml`)
   - Frontend: `test` (from `ci.yml`)

3. **Require branches to be up to date before merging**

4. **Do not allow bypassing the above settings**

## Apply via GitHub CLI

Use a JSON input file to avoid shell escaping issues:

```bash
# Backend — save as branch-protection.json:
# {
#   "required_status_checks": { "strict": true, "contexts": ["build"] },
#   "enforce_admins": true,
#   "required_pull_request_reviews": { "required_approving_review_count": 0 },
#   "restrictions": null
# }

gh api repos/hankimthuy/reflectly-be/branches/main/protection --method PUT --input branch-protection.json
gh api repos/hankimthuy/reflectly-fe/branches/main/protection --method PUT --input branch-protection-fe.json
# branch-protection-fe.json uses "contexts": ["test"]
```

> Requires `gh auth login` with admin access to the repository.

## Required GitHub Secrets checklist

See README in each repo for the full secrets list. After merging develop → main, verify all secrets exist before pushing to `main`.
