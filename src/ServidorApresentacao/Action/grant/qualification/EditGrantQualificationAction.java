/*
 * Created on 20/Dec/2003
 */

package ServidorApresentacao.Action.grant.qualification;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.validator.DynaValidatorForm;

import DataBeans.InfoCountry;
import DataBeans.InfoPerson;
import DataBeans.person.InfoQualification;
import Dominio.Country;
import ServidorAplicacao.IUserView;
import ServidorAplicacao.Servico.exceptions.FenixServiceException;
import ServidorApresentacao.Action.sop.utils.ServiceUtils;
import ServidorApresentacao.Action.sop.utils.SessionUtils;

/**
 * @author Barbosa
 * @author Pica
 *  
 */

public class EditGrantQualificationAction extends DispatchAction
{
	/*
	 * Fills the form with the correspondent data
	 */
	public ActionForward prepareEditGrantQualificationForm(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception
	{
		Integer idQualification = null;
		if (request.getParameter("idQualification") != null
			&& !request.getParameter("idQualification").equals(""))
			idQualification = new Integer(request.getParameter("idQualification"));

		DynaValidatorForm grantQualificationForm = (DynaValidatorForm) form;
		IUserView userView = SessionUtils.getUserView(request);

		if (idQualification != null) //Edit
		{
			try
			{
				//Read the contract
				Object[] args = { idQualification };
				InfoQualification infoGrantQualification =
					(InfoQualification) ServiceUtils.executeService(userView, "ReadQualification", args);

				//Populate the form
				setFormGrantQualification(grantQualificationForm, infoGrantQualification);

				request.setAttribute("idPerson", infoGrantQualification.getInfoPerson().getIdInternal());
				request.setAttribute("username", infoGrantQualification.getInfoPerson().getUsername());
				request.setAttribute("idInternal", request.getParameter("idInternal"));
			}
			catch (FenixServiceException e)
			{
				return setError(
					request,
					mapping,
					"errors.grant.qualification.read",
					"manage-grant-qualification",
					null);
			}
		}
		else //New
			{
			try
			{
				Integer idPerson = null;
				if (!request.getParameter("idPerson").equals(""))
					idPerson = new Integer(request.getParameter("idPerson"));
				grantQualificationForm.set("idPerson", idPerson);
				request.setAttribute("idPerson", idPerson);
				request.setAttribute("idInternal", request.getParameter("idInternal"));
				request.setAttribute("username", request.getParameter("username"));
			}
			catch (Exception e)
			{
				return setError(
					request,
					mapping,
					"errors.grant.unrecoverable",
					"manage-grant-qualification",
					null);
			}
		}

		List countryList = null;
		try
		{
			countryList =
				(List) ServiceUtils.executeService(
					SessionUtils.getUserView(request),
					"ReadAllCountries",
					null);
		}
		catch (Exception e)
		{
			return setError(
				request,
				mapping,
				"errors.grant.unrecoverable",
				"manage-grant-qualification",
				null);
		}

		//Adding a select country line to the list (presentation reasons)
		Country selectCountry = new Country();
		selectCountry.setIdInternal(null);
		selectCountry.setName("[Escolha um pa�s]");
		countryList.add(0, selectCountry);
		request.setAttribute("countryList", countryList);

		return mapping.findForward("edit-grant-qualification");
	}

	public ActionForward doEdit(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception
	{
		try
		{
			DynaValidatorForm editGrantQualificationForm = (DynaValidatorForm) form;
			InfoQualification infoGrantQualification = populateInfoFromForm(editGrantQualificationForm);

			request.setAttribute("idInternal", editGrantQualificationForm.get("idInternal"));
			request.setAttribute("idPerson", editGrantQualificationForm.get("idPerson"));
			request.setAttribute("username", editGrantQualificationForm.get("username"));

			Object[] args = { infoGrantQualification.getIdInternal(), infoGrantQualification };
			IUserView userView = SessionUtils.getUserView(request);
			ServiceUtils.executeService(userView, "EditQualification", args);
		}
		catch (FenixServiceException e)
		{
			return setError(request, mapping, "errors.grant.qualification.bd.create", null, null);
		}
		catch (Exception e)
		{
			return setError(request, mapping, "errors.grant.unrecoverable", null, null);
		}

		return mapping.findForward("manage-grant-qualification");
	}

	public ActionForward doDelete(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response)
		throws Exception
	{
		try
		{
			Integer idQualification = new Integer(request.getParameter("idQualification"));
			request.setAttribute("idInternal", request.getParameter("idInternal"));
			request.setAttribute("idPerson", request.getParameter("idPerson"));
			request.setAttribute("username", request.getParameter("username"));

			Object[] args = { idQualification };
			IUserView userView = SessionUtils.getUserView(request);
			ServiceUtils.executeService(userView, "DeleteQualification", args);
		}
		catch (FenixServiceException e)
		{
			return setError(request, mapping, "errors.grant.qualification.bd.delete", null, null);
		}
		catch (Exception e)
		{
			return setError(request, mapping, "errors.grant.unrecoverable", null, null);
		}
		return mapping.findForward("manage-grant-qualification");
	}

	/*
	 * Populates form from InfoContract
	 */
	private void setFormGrantQualification(
		DynaValidatorForm form,
		InfoQualification infoGrantQualification)
		throws Exception
	{
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		form.set("mark", infoGrantQualification.getMark());
		form.set("school", infoGrantQualification.getSchool());
		form.set("title", infoGrantQualification.getTitle());
		form.set("degree", infoGrantQualification.getDegree());
		if (infoGrantQualification.getDate() != null)
			form.set("qualificationDate", sdf.format(infoGrantQualification.getDate()));
		form.set("branch", infoGrantQualification.getBranch());
		form.set("specializationArea", infoGrantQualification.getSpecializationArea());
		form.set("degreeRecognition", infoGrantQualification.getDegreeRecognition());
		if (infoGrantQualification.getEquivalenceDate() != null)
			form.set("equivalenceDate", sdf.format(infoGrantQualification.getEquivalenceDate()));
		form.set("equivalenceSchool", infoGrantQualification.getEquivalenceSchool());
		form.set("idPerson", infoGrantQualification.getInfoPerson().getIdInternal());
		form.set("idQualification", infoGrantQualification.getIdInternal());
		form.set("username", infoGrantQualification.getInfoPerson().getUsername());
		if (infoGrantQualification.getInfoCountry() != null)
			form.set("country", infoGrantQualification.getInfoCountry().getIdInternal());
	}

	private InfoQualification populateInfoFromForm(DynaValidatorForm editGrantQualificationForm)
		throws Exception
	{
		InfoQualification infoQualification = new InfoQualification();
		InfoPerson infoPerson = new InfoPerson();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		if (editGrantQualificationForm.get("qualificationDate") != null
			&& !editGrantQualificationForm.get("qualificationDate").equals(""))
		{
			infoQualification.setDate(
				sdf.parse((String) editGrantQualificationForm.get("qualificationDate")));
		}
		if (editGrantQualificationForm.get("equivalenceDate") != null
			&& !editGrantQualificationForm.get("equivalenceDate").equals(""))
		{
			infoQualification.setEquivalenceDate(
				sdf.parse((String) editGrantQualificationForm.get("equivalenceDate")));
		} 
		if (editGrantQualificationForm.get("idQualification") != null
			&& !editGrantQualificationForm.get("idQualification").equals(""))
			infoQualification.setIdInternal((Integer) editGrantQualificationForm.get("idQualification"));
		if (editGrantQualificationForm.get("mark") != null)
			infoQualification.setMark((String) editGrantQualificationForm.get("mark"));
		infoQualification.setSchool((String) editGrantQualificationForm.get("school"));
		if (editGrantQualificationForm.get("title") != null)
			infoQualification.setTitle((String) editGrantQualificationForm.get("title"));
		infoQualification.setDegree((String) editGrantQualificationForm.get("degree"));
		if (editGrantQualificationForm.get("branch") != null)
			infoQualification.setBranch((String) editGrantQualificationForm.get("branch"));
		if (editGrantQualificationForm.get("specializationArea") != null)
			infoQualification.setSpecializationArea(
				(String) editGrantQualificationForm.get("specializationArea"));
		if (editGrantQualificationForm.get("degreeRecognition") != null)
			infoQualification.setDegreeRecognition(
				(String) editGrantQualificationForm.get("degreeRecognition"));
		if (editGrantQualificationForm.get("equivalenceSchool") != null)
			infoQualification.setEquivalenceSchool(
				(String) editGrantQualificationForm.get("equivalenceSchool"));
		infoPerson.setIdInternal((Integer) editGrantQualificationForm.get("idPerson"));
		infoQualification.setInfoPerson(infoPerson);
		InfoCountry infoCountry = new InfoCountry();
		if (((Integer) editGrantQualificationForm.get("country")).equals(new Integer(0)))
			infoCountry.setIdInternal(null);
		else
			infoCountry.setIdInternal((Integer) editGrantQualificationForm.get("country"));
		infoQualification.setInfoCountry(infoCountry);
		return infoQualification;
	}
	/*
	 * Sets an error to be displayed in the page and sets the mapping forward
	 */
	private ActionForward setError(
		HttpServletRequest request,
		ActionMapping mapping,
		String errorMessage,
		String forwardPage,
		Object actionArg)
	{
		ActionErrors errors = new ActionErrors();
		ActionError error = new ActionError(errorMessage, actionArg);
		errors.add(errorMessage, error);
		saveErrors(request, errors);

		if (forwardPage != null)
			return mapping.findForward(forwardPage);
		else
			return mapping.getInputForward();
	}
}
